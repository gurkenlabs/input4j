package de.gurkenlabs.litiengine.input.natives;


import de.gurkenlabs.litiengine.input.RawGamepad;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.foreign.ValueLayout.*;

public class DirectInputPlayground {
  public static final int DI8DEVCLASS_GAMECTRL = 4;

  public static final int DIRECTINPUT_VERSION = 0x0800;

  public static final int DI_OK = 0x00000000;

  public static final int DIERR_INVALIDPARAM = 0x80070057;

  public static final int DIERR_NOTINITIALIZED = 0x80070015;

  private static MethodHandle directInput8Create;

  private static MethodHandle getModuleHandle;

  static Map<IDirectInputDevice8, RawGamepad> deviceInstances = new ConcurrentHashMap<>();

  static {
    getModuleHandle = RuntimeHelper.downcallHandle("GetModuleHandleW",
            FunctionDescriptor.of(ADDRESS, ADDRESS));

    directInput8Create = RuntimeHelper.downcallHandle("DirectInput8Create",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
  }

  public static void main(String[] args) throws Throwable {
    enumDirectInput8Devices();
  }

  private static void enumDirectInput8Devices() throws Throwable {
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

      System.out.println("Found " + deviceInstances.size() + " gamepads.");
      for (var device : deviceInstances.entrySet()) {
        var directInputDevice = device.getKey();
        var gamepad = device.getValue();
        System.out.println("\t" + gamepad.instanceName());

        var deviceAddress = memorySession.allocate(JAVA_LONG.byteSize());
        var deviceGuidMemorySegment = memorySession.allocate(GUID.$LAYOUT);
        directInputDevice.deviceInstance.guidInstance.write(deviceGuidMemorySegment);

        if (directInput.CreateDevice(deviceGuidMemorySegment, deviceAddress) != DI_OK) {
          System.out.println("Device " + gamepad.instanceName() + " could not be created");
          continue;
        }
        directInputDevice.create(deviceAddress, memorySession);
        if (directInputDevice.EnumObjects(enumObjectsCallbackNative(memorySession), IDirectInputDevice8.DIDFT_BUTTON | IDirectInputDevice8.DIDFT_AXIS | IDirectInputDevice8.DIDFT_POV) != DI_OK) {
          System.out.println("Could not enumerate the device instance objects for " + gamepad.instanceName());
          continue;
        }
      }
    } catch (Exception e) {
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
  private static boolean enumDevicesCallback(MemoryAddress lpddiSegment, MemoryAddress pvRef) {
    try (var memorySession = MemorySession.openConfined()) {
      var deviceInstance = DIDEVICEINSTANCE.read(MemorySegment.ofAddress(lpddiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), memorySession));
      var name = new String(deviceInstance.tszInstanceName).trim();
      var product = new String(deviceInstance.tszProductName).trim();
      var type = DI8DEVTYPE.fromDwDevType(deviceInstance.dwDevType);

      // for now, we're only interested in gamepads, will add other types later
      if (type == DI8DEVTYPE.DI8DEVTYPE_GAMEPAD || type == DI8DEVTYPE.DI8DEVTYPE_JOYSTICK) {
        var gamepad = new RawGamepad(deviceInstance.guidInstance.toUUID(), deviceInstance.guidProduct.toUUID(), name, product);
        deviceInstances.put(new IDirectInputDevice8(deviceInstance), gamepad);
      } else {
        System.out.println("found device that is not a gamepad: " + name + "[" + type + "]");
      }
    }
    return true;
  }

  private static boolean enumObjectsCallback(MemoryAddress lpddoiSegment, MemoryAddress pvRef) {
    try (var memorySession = MemorySession.openConfined()) {
      var deviceObjectInstance = DIDEVICEOBJECTINSTANCE.read(MemorySegment.ofAddress(lpddoiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), memorySession));
      var name = new String(deviceObjectInstance.tszName).trim();
      var deviceObjectType = DI8DEVOBJECTTYPE.from(deviceObjectInstance.guidType);
      System.out.println("\t\t" + deviceObjectType + " (" + name + ") - " + deviceObjectInstance.dwOfs);
    }
    return true;
  }

  private static MemorySegment enumDevicesCallbackNative(MemorySession memorySession) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle onEnumDevices = MethodHandles.lookup()
            .findStatic(DirectInputPlayground.class, "enumDevicesCallback", MethodType.methodType(boolean.class, MemoryAddress.class, MemoryAddress.class));

    return Linker.nativeLinker().upcallStub(
            onEnumDevices, FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS), memorySession);
  }

  private static MemorySegment enumObjectsCallbackNative(MemorySession memorySession) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle onEnumDevices = MethodHandles.lookup()
            .findStatic(DirectInputPlayground.class, "enumObjectsCallback", MethodType.methodType(boolean.class, MemoryAddress.class, MemoryAddress.class));

    return Linker.nativeLinker().upcallStub(
            onEnumDevices, FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS), memorySession);
  }
}
