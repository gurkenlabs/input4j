package de.gurkenlabs.input4j.foreign.windows.dinput;


import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;

import java.awt.*;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

public final class DirectInputPlugin extends AbstractInputDevicePlugin {
  static final int DI8DEVCLASS_GAMECTRL = 4;

  static final int DIRECTINPUT_VERSION = 0x0800;

  final static int EVENT_QUEUE_DEPTH = 32;

  private static final MethodHandle directInput8Create;

  private static final MethodHandle getModuleHandle;

  private IDirectInput8 directInput;
  private final Map<String, IDirectInputDevice8> nativeDevices = new ConcurrentHashMap<>();
  private IDirectInputDevice8 currentDevice;

  /**
   * The components of the device that is currently being initialized.
   */
  private final Map<DIDEVICEOBJECTINSTANCE, InputComponent> currentComponents = new HashMap<>();

  private final Arena memoryArena = Arena.ofConfined();

  static {
    System.loadLibrary("Kernel32");
    System.loadLibrary("dinput8");

    getModuleHandle = downcallHandle("GetModuleHandleW", FunctionDescriptor.of(ADDRESS, ADDRESS));
    directInput8Create = downcallHandle("DirectInput8Create", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
  }

  @Override
  public void internalInitDevices(Frame owner) {
    try {
      this.initializeDirectInput();

      var hwnd = owner != null ? WindowHelper.getHWND(owner) : 0L;
      this.initializeDevices(hwnd);
      this.setDevices(this.nativeDevices.values().stream().map(d -> d.inputDevice).toList());
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public void close() {
    super.close();

    for (var device : this.nativeDevices.values()) {
      try {
        device.Unacquire();
        device.inputDevice.close();
      } catch (Throwable e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    this.nativeDevices.clear();
    this.currentDevice = null;
    this.currentComponents.clear();

    try {
      this.directInput.Release();
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    memoryArena.close();
  }

  @Override
  protected Collection<InputDevice> refreshInputDevices() {
    // TODO: implement refresh support
    // TODO: handle disconnect or permanent unavailability => retry X times => handle in hotplug thread if a device is unavailable throw it away
    return this.getAll();
  }

  private void initializeDirectInput() throws Throwable {
    var ppvOut = memoryArena.allocate(IDirectInput8.$LAYOUT);

    var moduleHandle = (MemorySegment) getModuleHandle.invoke(MemorySegment.NULL);
    var directInputCreateResult = (int) directInput8Create.invoke(
            moduleHandle,
            DIRECTINPUT_VERSION,
            IDirectInput8.IID_IDirectInput8W.write(memoryArena),
            ppvOut,
            MemorySegment.NULL);

    if (directInputCreateResult != Result.DI_OK) {
      log.log(Level.SEVERE, "Could not create DirectInput8: " + Result.toString(directInputCreateResult));
      return;
    }

    this.directInput = IDirectInput8.read(ppvOut, memoryArena);
  }

  private void initializeDevices(long hwnd) throws Throwable {

    // 1. enumerate input devices
    var enumDevicesResult = this.directInput.EnumDevices(DI8DEVCLASS_GAMECTRL, enumDevicesPointer(), IDirectInput8.DIEDFL_ALLDEVICES);
    if (enumDevicesResult != Result.DI_OK) {
      log.log(Level.SEVERE, "Could not enumerate DirectInput devices: " + Result.toString(enumDevicesResult));
      return;
    }

    // 2. create devices
    for (var device : this.nativeDevices.values()) {
      currentDevice = device;

      try {

        log.log(Level.FINE, "Found input device: " + device.inputDevice.getName());

        var deviceAddress = this.memoryArena.allocate(JAVA_LONG.byteSize());
        var deviceGuidMemorySegment = device.deviceInstance.guidInstance.write(this.memoryArena);

        if (this.directInput.CreateDevice(deviceGuidMemorySegment, deviceAddress) != Result.DI_OK) {
          log.log(Level.WARNING, "Device " + device.inputDevice.getName() + " could not be created");
          continue;
        }

        device.create(deviceAddress, this.memoryArena);

        // 3. enumerate the components
        var enumObjectsResult = device.EnumObjects(enumObjectsPointer(), IDirectInputDevice8.DIDFT_BUTTON | IDirectInputDevice8.DIDFT_AXIS | IDirectInputDevice8.DIDFT_POV);
        if (enumObjectsResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not enumerate the device instance objects for " + device.inputDevice.getName() + ": " + Result.toString(enumObjectsResult));
          continue;
        }

        // 4. prepare the device for retrieving data
        var deviceObjects = currentComponents.keySet().stream().toList();
        var dataFormat = defineDataFormat(deviceObjects, this.memoryArena);
        var setDataFormatResult = device.SetDataFormat(dataFormat);
        if (setDataFormatResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not set the data format for direct input device " + device.inputDevice.getName() + ": " + Result.toString(setDataFormatResult));
          continue;
        }

        // Some controllers, like the Xbox 360 controller, require exclusive acquisition.
        // This requires a valid owner instance instead of null (must be the focused window - can't be a hidden window).
        // When the owner is passed by the consuming application use foreground and exclusive mode; otherwise, use background and non-exclusive mode.
        int setCooperativeLevelResult;
        if (hwnd == 0) {
          setCooperativeLevelResult = device.SetCooperativeLevel(0, IDirectInputDevice8.DISCL_BACKGROUND | IDirectInputDevice8.DISCL_NONEXCLUSIVE);
        } else {
          setCooperativeLevelResult = device.SetCooperativeLevel(hwnd, IDirectInputDevice8.DISCL_FOREGROUND | IDirectInputDevice8.DISCL_EXCLUSIVE);
        }

        if (setCooperativeLevelResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not set the cooperative level for direct input device " + device.inputDevice.getName() + ": " + Result.toString(setCooperativeLevelResult));
          continue;
        }

        var setBufferSizeResult = device.SetProperty(IDirectInputDevice8.DIPROP_BUFFERSIZE, getDataBufferPropertyNative(this.memoryArena));
        if (setBufferSizeResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not set the buffer size for direct input device " + device.inputDevice.getName() + ": " + Result.toString(setBufferSizeResult));
          continue;
        }

        var acquireResult = device.Acquire();
        if (acquireResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not acquire direct input device " + device.inputDevice.getName() + ": " + Result.toString(acquireResult));
          continue;
        }

        device.nativeComponentCount = currentComponents.size();
        DirectInputVirtualComponentHandler.prepareVirtualComponents(device.inputDevice, currentComponents.values());
        device.deviceObjects = deviceObjects;
      } finally {
        this.currentDevice = null;
        this.currentComponents.clear();
      }
    }
  }

  private float[] pollDirectInputDevice(InputDevice inputDevice) {
    // find native DirectInputDevice and poll it
    var directInputDevice = this.nativeDevices.getOrDefault(inputDevice.getID(), null);
    if (directInputDevice == null) {
      log.log(Level.WARNING, "DirectInput device not found for input device " + inputDevice.getName());
      return new float[0];
    }
    var componentCount = directInputDevice.nativeComponentCount;
    var polledValues = new float[componentCount];

    try {
      final int DI_NOEFFECT = 1;

      var pollResult = directInputDevice.Poll();

      // there are multiple options why this result can happen:
      // 1. The physical controller has been disconnected or powered off.
      // 2. The device is operating in 'DISCL_FOREGROUND' mode and the application loses focus, the device state is no longer accessible.
      // 3. Another application or process may have reset the device, causing DirectInput to lose the device context.
      if (pollResult == Result.DIERR_INPUTLOST || pollResult == Result.DIERR_NOTACQUIRED) {
        var acquireResult = directInputDevice.Acquire();
        if (acquireResult != Result.DI_OK) {
          log.log(Level.WARNING, "Attempt to re-acquire failed for device " + directInputDevice.inputDevice.getName() + ": " + Result.toString(acquireResult));
          return polledValues;
        } else {
          pollResult = directInputDevice.Poll();
        }
      }

      if (pollResult != Result.DI_OK && pollResult != DI_NOEFFECT) {
        log.log(Level.WARNING, "Could not poll device " + inputDevice.getName() + ": " + Result.toString(pollResult));
        return polledValues;
      }

      // for details on the difference of GetDeviceState and GetDeviceData read http://doc.51windows.net/Directx9_SDK/input/using/devicedata/bufferedimmediatedata.htm
      var deviceStateResultSegment = this.memoryArena.allocate(MemoryLayout.sequenceLayout(componentCount, JAVA_INT));

      var getDeviceStateResult = directInputDevice.GetDeviceState((int) (componentCount * JAVA_INT.byteSize()), deviceStateResultSegment);
      if (getDeviceStateResult != Result.DI_OK) {
        log.log(Level.WARNING, "Could not get device state " + inputDevice.getName() + ": " + Result.toString(getDeviceStateResult));
      }

      for (int i = 0; i < componentCount; i++) {
        var convertedData = directInputDevice.deviceObjects.get(i).convertRawInputValue(deviceStateResultSegment.get(JAVA_INT, i * JAVA_INT.byteSize()));
        polledValues[i] = convertedData;
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return DirectInputVirtualComponentHandler.handlePolledValues(directInputDevice.inputDevice, polledValues);
  }

  private static MemorySegment defineDataFormat(List<DIDEVICEOBJECTINSTANCE> deviceObjects, Arena memoryArena) {
    var dataFormat = new DIDATAFORMAT();
    dataFormat.dwNumObjs = deviceObjects.size();
    dataFormat.dwDataSize = (int) (dataFormat.dwNumObjs * JAVA_INT.byteSize());

    /* Note: The DIDF_ABSAXIS flag is set if any axes are absolute.
     * DirectInput devices have their axis mode set per-device rather than per-axis.
     * To handle this, all axes are treated as absolute and adjusted for relative axes.
     *
     * Note: If all axes are relative (e.g., a mouse device), setting the DIDF_ABSAXIS flag
     * will result in incorrect axis values returned from GetDeviceData.
     */
    boolean allRelative = true;
    boolean hasAxis = false;
    for (var obj : deviceObjects) {
      if (obj.isAxis()) {
        hasAxis = true;
        if (!obj.isRelative()) {
          allRelative = false;
          break;
        }
      }
    }

    dataFormat.dwFlags = allRelative && hasAxis ? IDirectInputDevice8.DIDF_RELAXIS : IDirectInputDevice8.DIDF_ABSAXIS;

    var objectFormats = new DIOBJECTDATAFORMAT[dataFormat.dwNumObjs];

    for (int i = 0; i < deviceObjects.size(); i++) {
      var deviceObject = deviceObjects.get(i);
      var objectFormat = new DIOBJECTDATAFORMAT();
      var guidPointer = memoryArena.allocate(GUID.$LAYOUT);
      deviceObject.guidType.write(guidPointer);
      objectFormat.pguid = guidPointer;
      objectFormat.dwOfs = (int) (i * JAVA_INT.byteSize());
      objectFormat.dwType = deviceObject.dwType;
      objectFormat.dwFlags = deviceObject.dwFlags & (DIDEVICEOBJECTINSTANCE.DIDOI_ASPECTACCEL | DIDEVICEOBJECTINSTANCE.DIDOI_ASPECTFORCE | DIDEVICEOBJECTINSTANCE.DIDOI_ASPECTPOSITION | DIDEVICEOBJECTINSTANCE.DIDOI_ASPECTVELOCITY);

      objectFormats[i] = objectFormat;
    }

    dataFormat.setObjectDataFormats(objectFormats);

    var dataFormatMemorySegment = memoryArena.allocate(dataFormat.dwSize);
    dataFormat.write(dataFormatMemorySegment, memoryArena);

    return dataFormatMemorySegment;
  }

  private static MemorySegment getDataBufferPropertyNative(Arena memoryArena) {
    var propValue = new DIPROPDWORD();
    propValue.diph = new DIPROPHEADER();
    propValue.diph.dwSize = (int) DIPROPDWORD.$LAYOUT.byteSize();
    propValue.diph.dwObj = 0;
    propValue.diph.dwHow = DIPROPHEADER.DIPH_DEVICE;
    propValue.dwData = EVENT_QUEUE_DEPTH;

    var propertySegment = memoryArena.allocate(DIPROPDWORD.$LAYOUT);
    propValue.write(propertySegment);
    return propertySegment;
  }

  private void getProperties(DIDEVICEOBJECTINSTANCE deviceObject) throws Throwable {
    if (deviceObject.isAxis() && !deviceObject.isRelative()) {
      // fetch range property
      var rangeProp = new DIPROPRANGE();
      rangeProp.diph = new DIPROPHEADER();
      rangeProp.diph.dwSize = (int) DIPROPRANGE.$LAYOUT.byteSize();
      rangeProp.diph.dwObj = deviceObject.dwType;
      rangeProp.diph.dwHow = DIPROPHEADER.DIPH_BYID;

      var rangePropSegment = this.memoryArena.allocate(DIPROPRANGE.$LAYOUT);
      rangeProp.write(rangePropSegment);
      var rangePropResult = this.currentDevice.GetProperty(IDirectInputDevice8.DIPROP_RANGE, rangePropSegment);
      if (rangePropResult == Result.DI_OK) {
        var range = DIPROPRANGE.read(rangePropSegment);
        deviceObject.min = range.lMin;
        deviceObject.max = range.lMax;
      }

      // fetch deadzone property
      var deadZoneProp = new DIPROPDWORD();
      deadZoneProp.diph = new DIPROPHEADER();
      deadZoneProp.diph.dwSize = (int) DIPROPDWORD.$LAYOUT.byteSize();
      deadZoneProp.diph.dwObj = 0;
      deadZoneProp.diph.dwHow = DIPROPHEADER.DIPH_DEVICE;

      var deadZonePropSegment = this.memoryArena.allocate(DIPROPDWORD.$LAYOUT);
      deadZoneProp.write(deadZonePropSegment);
      var deadZoneResult = this.currentDevice.GetProperty(IDirectInputDevice8.DIPROP_DEADZONE, deadZonePropSegment);
      if (deadZoneResult == Result.DI_OK) {
        var deadZone = DIPROPDWORD.read(deadZonePropSegment);
        deviceObject.deadzone = deadZone.dwData;
      }
    } else {
      deviceObject.min = IDirectInputDevice8.DIPROPRANGE_NOMIN;
      deviceObject.max = IDirectInputDevice8.DIPROPRANGE_NOMAX;
    }
  }

  /**
   * This is called out of native code while enumerating the available devices.
   *
   * @param lpddiSegment The pointer to the {@link DIDEVICEINSTANCE} address.
   * @param pvRef        An application specific reference pointer (not used by our library).
   * @return True to indicate for the native code to continue with the enumeration otherwise false.
   */
  private boolean enumDeviceCallback(long lpddiSegment, long pvRef) {
    var deviceInstance = DIDEVICEINSTANCE.read(MemorySegment.ofAddress(lpddiSegment).reinterpret(DIDEVICEINSTANCE.$LAYOUT.byteSize(), memoryArena, null));
    var name = new String(deviceInstance.tszInstanceName).trim();
    var product = new String(deviceInstance.tszProductName).trim();

    // var type = DI8DEVTYPE.fromDwDevType(deviceInstance.dwDevType);
    var inputDevice = new InputDevice(deviceInstance.guidInstance.toString(), name, product, this::pollDirectInputDevice, null);
    this.nativeDevices.put(inputDevice.getID(), new IDirectInputDevice8(deviceInstance, inputDevice));

    return true;
  }

  /**
   * This is called out of native code while enumerating the available objects of a device.
   *
   * @param lpddoiSegment The pointer to the {@link DIDEVICEOBJECTINSTANCE} address.
   * @param pvRef         An application specific reference pointer (not used by our library).
   * @return True to indicate for the native code to continue with the enumeration otherwise false.
   */
  private boolean enumObjectCallback(long lpddoiSegment, long pvRef) {
    var deviceObjectInstance = DIDEVICEOBJECTINSTANCE.read(MemorySegment.ofAddress(lpddoiSegment).reinterpret(DIDEVICEOBJECTINSTANCE.$LAYOUT.byteSize(), memoryArena, null));

    var name = deviceObjectInstance.getName();
    var deviceObjectType = DI8DEVOBJECTTYPE.from(deviceObjectInstance.guidType);
    deviceObjectInstance.objectType = deviceObjectType;

    try {
      getProperties(deviceObjectInstance);
    } catch (Throwable throwable) {
      log.warning("Could not get properties of device object " + name);
    }

    var component = new InputComponent(this.currentDevice.inputDevice, deviceObjectInstance.getIdentifier(), name, deviceObjectInstance.isRelative());
    this.currentComponents.put(deviceObjectInstance, component);
    log.log(Level.FINE, "\t\t" + deviceObjectType + " (" + name + ") - " + deviceObjectInstance.dwOfs);
    return true;
  }

  /**
   * Passed to native code for callback on {@link #enumDeviceCallback(long, long)}
   **/
  private MemorySegment enumDevicesPointer() throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle enumDeviceMethodHandle = MethodHandles.lookup()
            .bind(this, "enumDeviceCallback", MethodType.methodType(boolean.class, long.class, long.class));

    return Linker.nativeLinker().upcallStub(
            enumDeviceMethodHandle, FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, JAVA_LONG), this.memoryArena);
  }

  /**
   * Passed to native code for callback on {@link #enumObjectCallback(long, long)}
   **/
  private MemorySegment enumObjectsPointer() throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle enumObjectMethodHandle = MethodHandles.lookup()
            .bind(this, "enumObjectCallback", MethodType.methodType(boolean.class, long.class, long.class));

    return Linker.nativeLinker().upcallStub(
            enumObjectMethodHandle, FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, JAVA_LONG), this.memoryArena);
  }
}
