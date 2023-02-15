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

  public static final int DIEDFL_ALLDEVICES = 0x00000000;

  public static final int DIRECTINPUT_VERSION = 0x0800;

  public static final int DI_OK = 0x00000000;

  public static final int DIERR_INVALIDPARAM = 0x80070057;

  private static final int DIENUM_CONTINUE = 1;

  private static final int DIENUM_STOP = 0;

  private static GUID IID_IDirectInput8W = new GUID(0xBF798031, (short) 0x483A, (short) 0x4DA2, (byte) 0xAA, (byte) 0x99, (byte) 0x5D, (byte) 0x64, (byte) 0xED, (byte) 0x36, (byte) 0x97, (byte) 0x00);

  private static MethodHandle directInput8Create;

  private static MethodHandle getModuleHandle;

  static Map<DIDEVICEINSTANCE, RawGamepad> deviceInstances = new ConcurrentHashMap<>();

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
      // Create a method handle to the Java function as a callback
      var riidltf = memorySession.allocate(GUID.$LAYOUT);
      IID_IDirectInput8W.write(riidltf);
      var ppvOut = memorySession.allocate(IDirectInput8W.$LAYOUT);

      if ((int) directInput8Create.invoke(getModuleHandle.invoke(MemoryAddress.NULL), DIRECTINPUT_VERSION, riidltf, ppvOut, MemoryAddress.NULL) != DI_OK) {
        System.out.println("oops");
        return;
      }

      var directInput8 = IDirectInput8W.read(ppvOut, memorySession);

      // Create a method handle to the Java function as a callback
      MethodHandle onEnumDevices = MethodHandles.lookup()
              .findStatic(DirectInputPlayground.class, "enumDevicesCallback", MethodType.methodType(long.class, MemoryAddress.class, MemoryAddress.class));

      MemorySegment onEnumDevicesNativeSymbol = Linker.nativeLinker().upcallStub(
              onEnumDevices, FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS), memorySession);

      var enumDevices = IDirectInput8W.Vtable.EnumDevices(directInput8.vtablePointer, memorySession);

      int result = enumDevices.apply(directInput8.vtablePointer, DI8DEVCLASS_GAMECTRL, onEnumDevicesNativeSymbol, MemoryAddress.NULL, DIEDFL_ALLDEVICES);
      if (result == DIERR_INVALIDPARAM) {
        System.out.println("DIERR_INVALIDPARAM: An invalid parameter was passed to the returning function, or the object was not in a state that permitted the function to be called.");
        return;
      }

      System.out.println("Found " + deviceInstances.size() + " gamepads.");
      for (var gamepad : deviceInstances.values()) {
        System.out.println("\t" + gamepad.instanceName());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static long enumDevicesCallback(MemoryAddress lpddiSegment, MemoryAddress pvRef) {
    try (var memorySession = MemorySession.openConfined()) {
      var device = DIDEVICEINSTANCE.read(MemorySegment.ofAddress(lpddiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), memorySession));
      var name = new String(device.tszInstanceName).trim();
      var product = new String(device.tszProductName).trim();
      var type = DI8DEVTYPE.fromDwDevType(device.dwDevType);

      // for now, we're only interested in gamepads, will add other types later
      if (type == DI8DEVTYPE.DI8DEVTYPE_GAMEPAD) {
        var gamepad = new RawGamepad(device.guidInstance.toUUID(), device.guidProduct.toUUID(), name, product);
        deviceInstances.put(device, gamepad);
      } else {
        System.out.println("found device that is not a gamepad: " + name + "[" + type + "]");
      }
    }
    return DIENUM_CONTINUE;
  }
}
