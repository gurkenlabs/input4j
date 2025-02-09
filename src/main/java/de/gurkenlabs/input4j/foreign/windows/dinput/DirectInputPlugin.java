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
import java.util.logging.Logger;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

/**
 * TODO: Implement hot swapping controllers
 */
public final class DirectInputPlugin extends AbstractInputDevicePlugin {
  private static final Logger log = Logger.getLogger(DirectInputPlugin.class.getName());

  static final int DI8DEVCLASS_GAMECTRL = 4;

  static final int DIRECTINPUT_VERSION = 0x0800;

  final static int EVENT_QUEUE_DEPTH = 32;

  private static final MethodHandle directInput8Create;

  private static final MethodHandle getModuleHandle;

  private IDirectInput8 directInput;
  private final Collection<IDirectInputDevice8> devices = ConcurrentHashMap.newKeySet();
  private IDirectInputDevice8 currentDevice;

  /**
   * The components of the device that is currently being initialized.
   */
  private final Map<DIDEVICEOBJECTINSTANCE, InputComponent> currentComponents = new HashMap<>();

  private final Arena memoryArena = Arena.ofConfined();

  static {
    System.loadLibrary("Kernel32");
    System.loadLibrary("dinput8");

    getModuleHandle = downcallHandle("GetModuleHandleW",
            FunctionDescriptor.of(ADDRESS, ADDRESS));

    directInput8Create = downcallHandle("DirectInput8Create",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
  }

  @Override
  public void internalInitDevices(Frame owner) {
    try {
      this.initializeDirectInput();

      var hwnd = owner != null ? WindowHelper.getHWND(owner) : 0L;
      this.initializeDevices(hwnd);
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public Collection<InputDevice> getAll() {
    return this.devices.stream().map(x -> x.inputDevice).toList();
  }

  @Override
  public void close() {
    for (var device : this.devices) {
      try {
        device.Unacquire();
        device.inputDevice.close();
      } catch (Throwable e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }

    this.devices.clear();
    this.currentDevice = null;
    this.currentComponents.clear();

    try {
      this.directInput.Release();
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    memoryArena.close();
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
    for (var device : this.devices) {
      currentDevice = device;

      try {

        log.log(Level.INFO, "Found input device: " + device.inputDevice.getInstanceName());

        var deviceAddress = this.memoryArena.allocate(JAVA_LONG.byteSize());
        var deviceGuidMemorySegment = device.deviceInstance.guidInstance.write(this.memoryArena);

        if (this.directInput.CreateDevice(deviceGuidMemorySegment, deviceAddress) != Result.DI_OK) {
          log.log(Level.WARNING, "Device " + device.inputDevice.getInstanceName() + " could not be created");
          continue;
        }

        device.create(deviceAddress, this.memoryArena);

        // 3. enumerate the components
        var enumObjectsResult = device.EnumObjects(enumObjectsPointer(), IDirectInputDevice8.DIDFT_BUTTON | IDirectInputDevice8.DIDFT_AXIS | IDirectInputDevice8.DIDFT_POV);
        if (enumObjectsResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not enumerate the device instance objects for " + device.inputDevice.getInstanceName() + ": " + Result.toString(enumObjectsResult));
          continue;
        }

        // TODO: In DirectInput, the D-Pad is reported as a single Hat Switch (angles or -1 for centered).
        //  This requires splitting the Hat Switch into components (DPAD_UP, DPAD_DOWN, etc.) in the unified model.
        //  We need to add 4 more button components to account for this
        //  It seems like we get the buttons 11, 12, 13, 14 and 15 that don't provide values but are reserved for individual DPAD values

        // TODO: In DirectInput, both triggers are mapped to the same Z-Axis, with LEFT_TRIGGER using the positive range (0 to 1)
        //  and RIGHT_TRIGGER using the negative range (-1 to 0). In Linux and XInput, the triggers are separate.
        //  We need to separate these to component into two virtual components that reflect the triggers individually

        // 4. enumerate the effects
        var enumEffectsResult = device.EnumEffects(enumEffectsPointer(), IDirectInputDevice8.DIEFT_ALL);
        if (enumEffectsResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not enumerate the device instance effects for " + device.inputDevice.getInstanceName() + ": " + Result.toString(enumEffectsResult));
          continue;
        }

        // 4. prepare the device for retrieving data
        var deviceObjects = currentComponents.keySet().stream().toList();
        var dataFormat = defineDataFormat(deviceObjects, this.memoryArena);
        var setDataFormatResult = device.SetDataFormat(dataFormat);
        if (setDataFormatResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not set the data format for direct input device " + device.inputDevice.getInstanceName() + ": " + Result.toString(setDataFormatResult));
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
          log.log(Level.WARNING, "Could not set the cooperative level for direct input device " + device.inputDevice.getInstanceName() + ": " + Result.toString(setCooperativeLevelResult));
          continue;
        }

        var setBufferSizeResult = device.SetProperty(IDirectInputDevice8.DIPROP_BUFFERSIZE, getDataBufferPropertyNative(this.memoryArena));
        if (setBufferSizeResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not set the buffer size for direct input device " + device.inputDevice.getInstanceName() + ": " + Result.toString(setBufferSizeResult));
          continue;
        }

        var acquireResult = device.Acquire();
        if (acquireResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not acquire direct input device " + device.inputDevice.getInstanceName() + ": " + Result.toString(acquireResult));
          continue;
        }

        device.inputDevice.setComponents(currentComponents.values());
        device.deviceObjects = deviceObjects;
      } finally {
        this.currentDevice = null;
        this.currentComponents.clear();
      }
    }
  }

  private float[] pollDirectInputDevice(InputDevice inputDevice) {
    var polledValues = new float[inputDevice.getComponents().size()];
    // find native DirectInputDevice and poll it
    var directInputDevice = this.devices.stream().filter(x -> x.inputDevice.equals(inputDevice)).findFirst().orElse(null);
    if (directInputDevice == null) {
      log.log(Level.WARNING, "DirectInput device not found for input device " + inputDevice.getInstanceName());
      return polledValues;
    }

    try {
      final int DI_NOEFFECT = 1;

      var pollResult = directInputDevice.Poll();

      // there are multiple options why this result can happen:
      // 1. The physical controller has been disconnected or powered off.
      // 2. The device is operating in 'DISCL_FOREGROUND' mode and the application loses focus, the device state is no longer accessible.
      // 3. Another application or process may have reset the device, causing DirectInput to lose the device context.
      if (pollResult == Result.DIERR_INPUTLOST || pollResult == Result.DIERR_NOTACQUIRED) {
        // TODO: handle disconnect or permanent unavailability => retry X times => handle in hotplug thread if a device is unavailable throw it away
        directInputDevice.Acquire();
      }
      if (pollResult != Result.DI_OK && pollResult != DI_NOEFFECT) {
        log.log(Level.WARNING, "Could not poll device " + inputDevice.getInstanceName() + ": " + Result.toString(pollResult));
        return polledValues;
      }

      // for details on the difference of GetDeviceState and GetDeviceData read http://doc.51windows.net/Directx9_SDK/input/using/devicedata/bufferedimmediatedata.htm
      var componentCount = inputDevice.getComponents().size();
      var deviceStateResultSegment = this.memoryArena.allocate(MemoryLayout.sequenceLayout(componentCount, JAVA_INT));

      var getDeviceStateResult = directInputDevice.GetDeviceState((int) (componentCount * JAVA_INT.byteSize()), deviceStateResultSegment);
      if (getDeviceStateResult != Result.DI_OK) {
        log.log(Level.WARNING, "Could not get device state " + inputDevice.getInstanceName() + ": " + Result.toString(getDeviceStateResult));
      }

      for (int i = 0; i < componentCount; i++) {
        var convertedData = directInputDevice.deviceObjects.get(i).convertRawInputValue(deviceStateResultSegment.get(JAVA_INT, i * JAVA_INT.byteSize()));
        polledValues[i] = convertedData;
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return polledValues;
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

  void getProperties(DIDEVICEOBJECTINSTANCE deviceObject) throws Throwable {
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
    var inputDevice = new InputDevice(name, product, this::pollDirectInputDevice, null);
    this.devices.add(new IDirectInputDevice8(deviceInstance, inputDevice));

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

  private boolean enumEffectCallback(long lpdeiSegment, long pvRef) {
    var effectInfo = DIEFFECTINFO.read(MemorySegment.ofAddress(lpdeiSegment).reinterpret(DIEFFECTINFO.$LAYOUT.byteSize(), memoryArena, null));
    var effectType = DIEFFECTTYPE.fromDwEffType(effectInfo.dwEffType);
    if (effectType == DIEFFECTTYPE.DIEFT_NONE) {
      // ignore empty effects
      return true;
    }

    var name = new String(effectInfo.tszName).trim();

    log.log(Level.FINE, "Found effect: " + name + " (Type: " + effectType + ")");

    // TODO: Implement support to create effects for the device. This requires DIEFFECT and other structs.
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

  private MemorySegment enumEffectsPointer() throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle enumObjectMethodHandle = MethodHandles.lookup()
            .bind(this, "enumEffectCallback", MethodType.methodType(boolean.class, long.class, long.class));

    return Linker.nativeLinker().upcallStub(
            enumObjectMethodHandle, FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, JAVA_LONG), this.memoryArena);
  }
}
