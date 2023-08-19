package de.gurkenlabs.litiengine.input.windows;


import de.gurkenlabs.litiengine.input.*;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.foreign.ValueLayout.*;

public final class DirectInputDeviceProvider implements InputDeviceProvider {
  private static final Logger log = Logger.getLogger(DirectInputDeviceProvider.class.getName());

  static final int DI8DEVCLASS_GAMECTRL = 4;

  static final int DIRECTINPUT_VERSION = 0x0800;

  final static int EVENT_QUEUE_DEPTH = 32;

  private static final MethodHandle directInput8Create;

  private static final MethodHandle getModuleHandle;

  private final Collection<IDirectInputDevice8> devices = ConcurrentHashMap.newKeySet();

  private final Map<DIDEVICEOBJECTINSTANCE, DeviceComponent> currentComponents = new HashMap<>();

  private final Arena memoryArea = Arena.openConfined();

  static {
    System.loadLibrary("Kernel32");
    System.loadLibrary("dinput8");

    getModuleHandle = downcallHandle("GetModuleHandleW",
            FunctionDescriptor.of(ADDRESS, ADDRESS));

    directInput8Create = downcallHandle("DirectInput8Create",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
  }

  @Override
  public void collectDevices() {
    this.enumDirectInput8Devices();
  }

  @Override
  public Collection<InputDevice> getDevices() {
    return this.devices.stream().map(x -> x.inputDevice).toList();
  }

  @Override
  public void close() {
    for (var device : this.devices) {
      try {
        device.Unacquire();
      } catch (Throwable e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
    return SymbolLookup.loaderLookup().find(name).or(() -> Linker.nativeLinker().defaultLookup().find(name)).
            map(addr -> Linker.nativeLinker().downcallHandle(addr, fdesc)).
            orElse(null);
  }

  static MethodHandle downcallHandle(MemorySegment address, FunctionDescriptor fdesc) {
    return Linker.nativeLinker().downcallHandle(address, fdesc);
  }

  private void enumDirectInput8Devices() {
    try {
      // 1. Initialize DirectInput
      var ppvOut = memoryArea.allocate(IDirectInput8.$LAYOUT);

      var moduleHandle = (MemorySegment) getModuleHandle.invoke(MemorySegment.NULL);
      var directInputCreateResult = (int) directInput8Create.invoke(
              moduleHandle,
              DIRECTINPUT_VERSION,
              IDirectInput8.IID_IDirectInput8W.write(memoryArea),
              ppvOut,
              MemorySegment.NULL);


      if (directInputCreateResult != Result.DI_OK) {
        log.log(Level.SEVERE, "Could not create DirectInput8: " + Result.toString(directInputCreateResult));
        return;
      }

      var directInput = IDirectInput8.read(ppvOut);

      // 2. enumerate input devices
      var enumDevicesResult = directInput.EnumDevices(DI8DEVCLASS_GAMECTRL, enumDevicesCallbackNative(), IDirectInput8.DIEDFL_ALLDEVICES);
      if (enumDevicesResult != Result.DI_OK) {
        log.log(Level.SEVERE, "Could not enumerate DirectInput devices: " + Result.toString(enumDevicesResult));
        return;
      }

      // 3. create devices
      for (var device : this.devices) {
        currentComponents.clear();
        log.log(Level.INFO, "Found input device: " + device.inputDevice.getInstanceName());

        var deviceAddress = memoryArea.allocate(JAVA_LONG.byteSize());
        var deviceGuidMemorySegment = device.deviceInstance.guidInstance.write(memoryArea);

        if (directInput.CreateDevice(deviceGuidMemorySegment, deviceAddress) != Result.DI_OK) {
          log.log(Level.WARNING, "Device " + device.inputDevice.getInstanceName() + " could not be created");
          continue;
        }

        device.create(deviceAddress, this.memoryArea.scope());

        // 4. enumerate the components
        var enumObjectsResult = device.EnumObjects(enumObjectsCallbackNative(), IDirectInputDevice8.DIDFT_BUTTON | IDirectInputDevice8.DIDFT_AXIS | IDirectInputDevice8.DIDFT_POV);
        if (enumObjectsResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not enumerate the device instance objects for " + device.inputDevice.getInstanceName() + ": " + Result.toString(enumObjectsResult));
          continue;
        }

        // 5. prepare the device for retrieving data
        var dataFormat = defineDataFormat(currentComponents.keySet().stream().toList(), this.memoryArea);
        var setDataFormatResult = device.SetDataFormat(dataFormat);
        if (setDataFormatResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not set the data format for direct input device " + device.inputDevice.getInstanceName() + ": " + Result.toString(setDataFormatResult));
          continue;
        }

        var setCooperativeLevelResult = device.SetCooperativeLevel(MemorySegment.NULL, IDirectInputDevice8.DISCL_BACKGROUND | IDirectInputDevice8.DISCL_NONEXCLUSIVE);
        if (setCooperativeLevelResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not set the cooperative level for direct input device " + device.inputDevice.getInstanceName() + ": " + Result.toString(setCooperativeLevelResult));
          continue;
        }

        var setBufferSizeResult = device.SetProperty(IDirectInputDevice8.DIPROP_BUFFERSIZE, getDataBufferPropertyNative(this.memoryArea));
        if (setBufferSizeResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not set the buffer size for direct input device " + device.inputDevice.getInstanceName() + ": " + Result.toString(setBufferSizeResult));
          continue;
        }

        var acquireResult = device.Acquire();
        if (acquireResult != Result.DI_OK) {
          log.log(Level.WARNING, "Could not acquire direct input device " + device.inputDevice.getInstanceName() + ": " + Result.toString(acquireResult));
          continue;
        }

        device.inputDevice.addComponents(currentComponents.values());
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

  }

  private void pollDirectInputDevice(InputDevice inputDevice) {
    // find native DirectInputDevice and poll it
    var directInputDevice = this.devices.stream().filter(x -> x.inputDevice.equals(inputDevice)).findFirst().orElse(null);
    if (directInputDevice == null) {
      log.log(Level.WARNING, "DirectInput device not found for input device " + inputDevice.getInstanceName());
      return;
    }

    try {
      final int DI_NOEFFECT = 1;

      var pollResult = directInputDevice.Poll();
      if (pollResult != Result.DI_OK && pollResult != DI_NOEFFECT) {
        log.log(Level.WARNING, "Could not poll device " + inputDevice.getInstanceName() + ": " + Result.toString(pollResult));
        return;
      }

      // for details on the difference of GetDeviceState and GetDeviceData read http://doc.51windows.net/Directx9_SDK/input/using/devicedata/bufferedimmediatedata.htm
      var componentCount = inputDevice.getComponents().size();
      if(directInputDevice.deviceInstance.deviceStateResultSegment == null){
        directInputDevice.deviceInstance.deviceStateResultSegment = this.memoryArea.allocateArray(JAVA_INT, componentCount);
      }

      var getDeviceStateResult = directInputDevice.GetDeviceState((int) (componentCount * JAVA_INT.byteSize()), directInputDevice.deviceInstance.deviceStateResultSegment);
      if (getDeviceStateResult != Result.DI_OK) {
        log.log(Level.WARNING, "Could not get device state " + inputDevice.getInstanceName() + ": " + Result.toString(getDeviceStateResult));
      }

      for (int i = 0; i < componentCount; i++) {
        inputDevice.getComponents().get(i).setValue(directInputDevice.deviceInstance.deviceStateResultSegment.get(JAVA_INT, i * JAVA_INT.byteSize()));
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * This is called out of native code while enumerating the available devices.
   *
   * @param lpddiSegment The pointer to the {@link DIDEVICEINSTANCE} address.
   * @param pvRef        An application specific reference pointer (not used by our library).
   * @return True to indicate for the native code to continue with the enumeration otherwise false.
   */
  private boolean enumDevicesCallback(long lpddiSegment, long pvRef) {
    var deviceInstance = DIDEVICEINSTANCE.read(MemorySegment.ofAddress(lpddiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), memoryArea.scope()));
    var name = new String(deviceInstance.tszInstanceName).trim();
    var product = new String(deviceInstance.tszProductName).trim();
    var type = DI8DEVTYPE.fromDwDevType(deviceInstance.dwDevType);

    // for now, we're only interested in gamepads, will add other types later
    if (type == DI8DEVTYPE.DI8DEVTYPE_GAMEPAD || type == DI8DEVTYPE.DI8DEVTYPE_JOYSTICK) {
      var inputDevice = new InputDevice(deviceInstance.guidInstance.toUUID(), deviceInstance.guidProduct.toUUID(), name, product, this::pollDirectInputDevice);
      this.devices.add(new IDirectInputDevice8(deviceInstance, inputDevice));
    } else {
      log.log(Level.WARNING, "found device that is not a gamepad or joystick: " + name + "[" + type + "]");
    }

    return true;
  }

  /**
   * This is called out of native code while enumerating the available objects of a device.
   *
   * @param lpddoiSegment The pointer to the {@link DIDEVICEOBJECTINSTANCE} address.
   * @param pvRef         An application specific reference pointer (not used by our library).
   * @return True to indicate for the native code to continue with the enumeration otherwise false.
   */
  private boolean enumObjectsCallback(long lpddoiSegment, long pvRef) {
    var deviceObjectInstance = DIDEVICEOBJECTINSTANCE.read(MemorySegment.ofAddress(lpddoiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), this.memoryArea.scope()));
    var name = new String(deviceObjectInstance.tszName).trim();
    var deviceObjectType = DI8DEVOBJECTTYPE.from(deviceObjectInstance.guidType);

    var component = new DeviceComponent(ComponentType.valueOf(deviceObjectType.name()), name);
    this.currentComponents.put(deviceObjectInstance, component);
    log.log(Level.FINE, "\t\t" + deviceObjectType + " (" + name + ") - " + deviceObjectInstance.dwOfs);
    return true;
  }

  private static MemorySegment defineDataFormat(List<DIDEVICEOBJECTINSTANCE> deviceObjects, Arena memoryArea) {
    var dataFormat = new DIDATAFORMAT();
    dataFormat.dwNumObjs = deviceObjects.size();
    dataFormat.dwDataSize = (int) (dataFormat.dwNumObjs * JAVA_INT.byteSize());
    dataFormat.dwFlags = IDirectInputDevice8.DIDF_ABSAXIS; // TODO: Evaluate if there is any relative axis

    var objectFormats = new DIOBJECTDATAFORMAT[dataFormat.dwNumObjs];

    for (int i = 0; i < deviceObjects.size(); i++) {
      var deviceObject = deviceObjects.get(i);
      var objectFormat = new DIOBJECTDATAFORMAT();
      var guidPointer = memoryArea.allocate(GUID.$LAYOUT);
      deviceObject.guidType.write(guidPointer);
      objectFormat.pguid = guidPointer;
      objectFormat.dwOfs = (int) (i * JAVA_INT.byteSize());
      objectFormat.dwType = deviceObject.dwType;
      objectFormat.dwFlags = deviceObject.dwFlags & (DIDEVICEOBJECTINSTANCE.DIDOI_ASPECTACCEL | DIDEVICEOBJECTINSTANCE.DIDOI_ASPECTFORCE | DIDEVICEOBJECTINSTANCE.DIDOI_ASPECTPOSITION | DIDEVICEOBJECTINSTANCE.DIDOI_ASPECTVELOCITY);

      objectFormats[i] = objectFormat;
    }

    dataFormat.setObjectDataFormats(objectFormats);

    var dataFormatMemorySegment = memoryArea.allocate(dataFormat.dwSize);
    dataFormat.write(dataFormatMemorySegment, memoryArea);

    return dataFormatMemorySegment;
  }

  // passed to native code for callback
  private MemorySegment enumDevicesCallbackNative() throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle onEnumDevices = MethodHandles.lookup()
            .bind(this, "enumDevicesCallback", MethodType.methodType(boolean.class, long.class, long.class));

    return Linker.nativeLinker().upcallStub(
            onEnumDevices, FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, JAVA_LONG), this.memoryArea.scope());
  }

  // passed to native code for callback
  private MemorySegment enumObjectsCallbackNative() throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle onEnumDevices = MethodHandles.lookup()
            .bind(this, "enumObjectsCallback", MethodType.methodType(boolean.class, long.class, long.class));

    return Linker.nativeLinker().upcallStub(
            onEnumDevices, FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, JAVA_LONG), this.memoryArea.scope());
  }

  private static MemorySegment getDataBufferPropertyNative(Arena memoryArea) {
    var propValue = new DIPROPDWORD();
    propValue.diph = new DIPROPHEADER();
    propValue.diph.dwObj = 0;
    propValue.diph.dwHow = DIPROPHEADER.DIPH_DEVICE;
    propValue.dwData = EVENT_QUEUE_DEPTH;

    var propertySegment = memoryArea.allocate(DIPROPDWORD.$LAYOUT);
    propValue.write(propertySegment);
    return propertySegment;
  }

  private static class Result {
    static final int DI_OK = 0x00000000;

    static final int DIERR_INVALIDPARAM = 0x80070057;
    static final int DIERR_NOTINITIALIZED = 0x80070015;
    static final int DI_BUFFEROVERFLOW = 0x00000001;
    static final int DIERR_INPUTLOST = 0x8007001E;
    static final int DIERR_NOTACQUIRED = 0x8007000C;
    static final int DIERR_OTHERAPPHASPRIO = 0x80070005;

    static String toString(int HRESULT) {
      var hexResult = String.format("%08X", HRESULT);
      return switch (HRESULT) {
        case DI_OK -> "DI_OK: " + hexResult;
        case DIERR_INVALIDPARAM -> "DIERR_INVALIDPARAM: " + hexResult;
        case DIERR_NOTINITIALIZED -> "DIERR_NOTINITIALIZED: " + hexResult;
        case DI_BUFFEROVERFLOW -> "DI_BUFFEROVERFLOW: " + hexResult;
        case DIERR_INPUTLOST -> "DIERR_INPUTLOST: " + hexResult;
        case DIERR_NOTACQUIRED -> "DIERR_NOTACQUIRED: " + hexResult;
        case DIERR_OTHERAPPHASPRIO -> "DIERR_OTHERAPPHASPRIO: " + hexResult;

        default -> hexResult;
      };

    }
  }
}
