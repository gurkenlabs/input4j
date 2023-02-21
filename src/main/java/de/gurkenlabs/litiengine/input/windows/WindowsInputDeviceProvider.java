package de.gurkenlabs.litiengine.input.windows;


import de.gurkenlabs.litiengine.input.DeviceComponent;
import de.gurkenlabs.litiengine.input.ComponentType;
import de.gurkenlabs.litiengine.input.InputDeviceProvider;
import de.gurkenlabs.litiengine.input.InputDevice;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.foreign.ValueLayout.*;

final class WindowsInputDeviceProvider implements InputDeviceProvider {
  public static final int DI8DEVCLASS_GAMECTRL = 4;

  public static final int DIRECTINPUT_VERSION = 0x0800;

  public static final int DI_OK = 0x00000000;

  public static final int DIERR_INVALIDPARAM = 0x80070057;

  public static final int DIERR_NOTINITIALIZED = 0x80070015;

  private final static Linker NATIVE_LINKER = Linker.nativeLinker();

  private final static SymbolLookup SYMBOL_LOOKUP;

  private static MethodHandle directInput8Create;

  private static MethodHandle getModuleHandle;

  private final Map<IDirectInputDevice8, InputDevice> deviceInstances = new ConcurrentHashMap<>();

  private final ArrayList<DeviceComponent> currentComponents = new ArrayList<>();

  static {
    System.loadLibrary("Kernel32");
    System.loadLibrary("dinput8");

    SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
    SYMBOL_LOOKUP = name -> loaderLookup.lookup(name).or(() -> NATIVE_LINKER.defaultLookup().lookup(name));

    getModuleHandle = downcallHandle("GetModuleHandleW",
            FunctionDescriptor.of(ADDRESS, ADDRESS));

    directInput8Create = downcallHandle("DirectInput8Create",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
  }

  static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
    return SYMBOL_LOOKUP.lookup(name).
            map(addr -> NATIVE_LINKER.downcallHandle(addr, fdesc)).
            orElse(null);
  }

  static MethodHandle downcallHandle(MemoryAddress address, FunctionDescriptor fdesc) {
    return NATIVE_LINKER.downcallHandle(address, fdesc);
  }

  @Override
  public void collectDevices() {
    this.enumDirectInput8Devices();
  }

  @Override
  public Collection<InputDevice> getDevices() {
    return this.deviceInstances.values();
  }

  private void enumDirectInput8Devices() {
    try (var memorySession = MemorySession.openConfined()) {
      var riidltf = memorySession.allocate(GUID.$LAYOUT);
      IDirectInput8.IID_IDirectInput8W.write(riidltf);
      var ppvOut = memorySession.allocate(IDirectInput8.$LAYOUT);

      if ((int) directInput8Create.invoke(getModuleHandle.invoke(MemoryAddress.NULL), DIRECTINPUT_VERSION, riidltf, ppvOut, MemoryAddress.NULL) != DI_OK) {
        System.out.println("oops");
        return;
      }

      var directInput = IDirectInput8.read(ppvOut, memorySession);
      var result = directInput.EnumDevices(DI8DEVCLASS_GAMECTRL, enumDevicesCallbackNative(memorySession), IDirectInput8.DIEDFL_ALLDEVICES);
      if (result != DI_OK) {
        System.out.println("Could not enumerate direct input devices (" + result);
        return;
      }

      System.out.println("Found " + this.deviceInstances.size() + " gamepads.");
      for (var device : this.deviceInstances.entrySet()) {
        currentComponents.clear();
        var directInputDevice = device.getKey();
        var inputDevice = device.getValue();
        System.out.println("\t" + inputDevice.getInstanceName());

        var deviceAddress = memorySession.allocate(JAVA_LONG.byteSize());
        var deviceGuidMemorySegment = memorySession.allocate(GUID.$LAYOUT);
        directInputDevice.deviceInstance.guidInstance.write(deviceGuidMemorySegment);

        if (directInput.CreateDevice(deviceGuidMemorySegment, deviceAddress) != DI_OK) {
          System.out.println("Device " + inputDevice.getInstanceName() + " could not be created");
          continue;
        }
        directInputDevice.create(deviceAddress, memorySession);
        if (directInputDevice.EnumObjects(enumObjectsCallbackNative(memorySession), IDirectInputDevice8.DIDFT_BUTTON | IDirectInputDevice8.DIDFT_AXIS | IDirectInputDevice8.DIDFT_POV) != DI_OK) {
          System.out.println("Could not enumerate the device instance objects for " + inputDevice.getInstanceName());
          continue;
        }

        inputDevice.addComponents(currentComponents);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   * This is called out of native code while enumerating the available devices.
   *
   * @param lpddiSegment The pointer to the {@link DIDEVICEINSTANCE} address.
   * @param pvRef
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
        System.out.println("found device that is not a gamepad or joystick: " + name + "[" + type + "]");
      }
    }
    return true;
  }

  private void pollInputDevice(InputDevice inputDevice) {
    // find native DirectInputDevice and poll it
    // this.deviceInstances.
  }

  private boolean enumObjectsCallback(MemoryAddress lpddoiSegment, MemoryAddress pvRef) {
    try (var memorySession = MemorySession.openConfined()) {
      var deviceObjectInstance = DIDEVICEOBJECTINSTANCE.read(MemorySegment.ofAddress(lpddoiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), memorySession));
      var name = new String(deviceObjectInstance.tszName).trim();
      var deviceObjectType = DI8DEVOBJECTTYPE.from(deviceObjectInstance.guidType);

      var component = new DeviceComponent(ComponentType.valueOf(deviceObjectType.name()), name);
      this.currentComponents.add(component);
      System.out.println("\t\t" + deviceObjectType + " (" + name + ") - " + deviceObjectInstance.dwOfs);
    }
    return true;
  }

  private MemorySegment enumDevicesCallbackNative(MemorySession memorySession) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle onEnumDevices = MethodHandles.lookup()
            .bind(this, "enumDevicesCallback", MethodType.methodType(boolean.class, MemoryAddress.class, MemoryAddress.class));

    return Linker.nativeLinker().upcallStub(
            onEnumDevices, FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS), memorySession);
  }

  private MemorySegment enumObjectsCallbackNative(MemorySession memorySession) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle onEnumDevices = MethodHandles.lookup()
            .bind(this, "enumObjectsCallback", MethodType.methodType(boolean.class, MemoryAddress.class, MemoryAddress.class));

    return Linker.nativeLinker().upcallStub(
            onEnumDevices, FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS), memorySession);
  }
}
