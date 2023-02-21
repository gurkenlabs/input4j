package de.gurkenlabs.litiengine.input.windows;


import de.gurkenlabs.litiengine.input.*;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.foreign.ValueLayout.*;

final class DirectInputDeviceProvider implements InputDeviceProvider {
  private static final Logger log = Logger.getLogger(DirectInputDeviceProvider.class.getName());

  public static final int DI8DEVCLASS_GAMECTRL = 4;

  public static final int DIRECTINPUT_VERSION = 0x0800;

  public static final int DI_OK = 0x00000000;

  public static final int DIERR_INVALIDPARAM = 0x80070057;

  public static final int DIERR_NOTINITIALIZED = 0x80070015;

  private static MethodHandle directInput8Create;

  private static MethodHandle getModuleHandle;

  private final Map<IDirectInputDevice8, InputDevice> deviceInstances = new ConcurrentHashMap<>();

  private final ArrayList<DeviceComponent> currentComponents = new ArrayList<>();

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
    return this.deviceInstances.values();
  }

  static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
    return SymbolLookup.loaderLookup().lookup(name).or(() -> Linker.nativeLinker().defaultLookup().lookup(name)).
            map(addr -> Linker.nativeLinker().downcallHandle(addr, fdesc)).
            orElse(null);
  }

  static MethodHandle downcallHandle(MemoryAddress address, FunctionDescriptor fdesc) {
    return Linker.nativeLinker().downcallHandle(address, fdesc);
  }

  private void enumDirectInput8Devices() {
    try (var memorySession = MemorySession.openConfined()) {
      // 1. Initialize DirectInput
      var riidltf = memorySession.allocate(GUID.$LAYOUT);
      IDirectInput8.IID_IDirectInput8W.write(riidltf);
      var ppvOut = memorySession.allocate(IDirectInput8.$LAYOUT);

      var directInputCreateResult = (int) directInput8Create.invoke(getModuleHandle.invoke(MemoryAddress.NULL), DIRECTINPUT_VERSION, riidltf, ppvOut, MemoryAddress.NULL);
      if (directInputCreateResult != DI_OK) {
        log.log(Level.SEVERE, "Could not create DirectInput8: " + String.format("%08X", directInputCreateResult));
        return;
      }

      var directInput = IDirectInput8.read(ppvOut, memorySession);

      // 2. enumerate input devices
      var enumDevicesResult = directInput.EnumDevices(DI8DEVCLASS_GAMECTRL, enumDevicesCallbackNative(memorySession), IDirectInput8.DIEDFL_ALLDEVICES);
      if (enumDevicesResult != DI_OK) {
        log.log(Level.SEVERE, "Could not enumerate DirectInput devices: " + String.format("%08X", enumDevicesResult));
        return;
      }

      // 3. create devices and enumerate their components
      for (var device : this.deviceInstances.entrySet()) {
        currentComponents.clear();
        var directInputDevice = device.getKey();
        var inputDevice = device.getValue();
        log.log(Level.INFO, "Found input device: " + inputDevice.getInstanceName());

        var deviceAddress = memorySession.allocate(JAVA_LONG.byteSize());
        var deviceGuidMemorySegment = memorySession.allocate(GUID.$LAYOUT);
        directInputDevice.deviceInstance.guidInstance.write(deviceGuidMemorySegment);

        if (directInput.CreateDevice(deviceGuidMemorySegment, deviceAddress) != DI_OK) {
          log.log(Level.WARNING, "Device " + inputDevice.getInstanceName() + " could not be created");
          continue;
        }
        directInputDevice.create(deviceAddress, memorySession);
        if (directInputDevice.EnumObjects(enumObjectsCallbackNative(memorySession), IDirectInputDevice8.DIDFT_BUTTON | IDirectInputDevice8.DIDFT_AXIS | IDirectInputDevice8.DIDFT_POV) != DI_OK) {
          log.log(Level.WARNING, "Could not enumerate the device instance objects for " + inputDevice.getInstanceName());
          continue;
        }

        inputDevice.addComponents(currentComponents);
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private void pollInputDevice(InputDevice inputDevice) {
    // find native DirectInputDevice and poll it
    // this.deviceInstances.
  }

  /**
   * This is called out of native code while enumerating the available devices.
   *
   * @param lpddiSegment The pointer to the {@link DIDEVICEINSTANCE} address.
   * @param pvRef        An application specific reference pointer (not used by our library).
   * @return True to indicate for the native code to continue with the enumeration otherwise false.
   */
  private boolean enumDevicesCallback(MemoryAddress lpddiSegment, MemoryAddress pvRef) {
    try (var memorySession = MemorySession.openConfined()) {
      var deviceInstance = DIDEVICEINSTANCE.read(MemorySegment.ofAddress(lpddiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), memorySession));
      var name = new String(deviceInstance.tszInstanceName).trim();
      var product = new String(deviceInstance.tszProductName).trim();
      var type = DI8DEVTYPE.fromDwDevType(deviceInstance.dwDevType);

      // for now, we're only interested in gamepads, will add other types later
      if (type == DI8DEVTYPE.DI8DEVTYPE_GAMEPAD || type == DI8DEVTYPE.DI8DEVTYPE_JOYSTICK) {
        var inputDevice = new InputDevice(deviceInstance.guidInstance.toUUID(), deviceInstance.guidProduct.toUUID(), name, product, this::pollInputDevice);
        this.deviceInstances.put(new IDirectInputDevice8(deviceInstance), inputDevice);
      } else {
        log.log(Level.WARNING, "found device that is not a gamepad or joystick: " + name + "[" + type + "]");
      }
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
  private boolean enumObjectsCallback(MemoryAddress lpddoiSegment, MemoryAddress pvRef) {
    try (var memorySession = MemorySession.openConfined()) {
      var deviceObjectInstance = DIDEVICEOBJECTINSTANCE.read(MemorySegment.ofAddress(lpddoiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), memorySession));
      var name = new String(deviceObjectInstance.tszName).trim();
      var deviceObjectType = DI8DEVOBJECTTYPE.from(deviceObjectInstance.guidType);

      var component = new DeviceComponent(ComponentType.valueOf(deviceObjectType.name()), name);
      this.currentComponents.add(component);
      log.log(Level.FINE, "\t\t" + deviceObjectType + " (" + name + ") - " + deviceObjectInstance.dwOfs);
    }
    return true;
  }

  // passed to native code for callback
  private MemorySegment enumDevicesCallbackNative(MemorySession memorySession) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle onEnumDevices = MethodHandles.lookup()
            .bind(this, "enumDevicesCallback", MethodType.methodType(boolean.class, MemoryAddress.class, MemoryAddress.class));

    return Linker.nativeLinker().upcallStub(
            onEnumDevices, FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS), memorySession);
  }

  // passed to native code for callback
  private MemorySegment enumObjectsCallbackNative(MemorySession memorySession) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle onEnumDevices = MethodHandles.lookup()
            .bind(this, "enumObjectsCallback", MethodType.methodType(boolean.class, MemoryAddress.class, MemoryAddress.class));

    return Linker.nativeLinker().upcallStub(
            onEnumDevices, FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS), memorySession);
  }
}
