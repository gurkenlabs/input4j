package de.gurkenlabs.litiengine.input.natives;


import de.gurkenlabs.litiengine.input.RawGamepad;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Objects;

import static java.lang.foreign.ValueLayout.*;

public class Program {
  public static int INVALID_HANDLE_VALUE = -1;
  public static final int DIGCF_PRESENT = 2;

  public static final int DIGCF_DEVICEINTERFACE = 16;

  public static final int SPDRP_DEVICEDESC = 0x00000000;

  public static final int SPDRP_CLASS = 0x00000007;
  public static final int SPDRP_DRIVER = 0x00000009;

  public static final int REG_SZ = 1;

  public static final int DI8DEVCLASS_GAMECTRL = 4;

  public static final int DIEDFL_ALLDEVICES = 0x00000000;

  public static final int DIRECTINPUT_VERSION = 0x0800;

  public static final int DI_OK = 0x00000000;

  public static final int DIERR_INVALIDPARAM = 0x80070057;

  private static final int DIENUM_CONTINUE = 1;

  private static final int DIENUM_STOP = 0;

  static GUID GUID_DEVINTERFACE_HID = new GUID(0x4d1e55b2,
          (short) 0xf16f,
          (short) 0x11cf,
          (byte) 0x88, (byte) 0xcb, (byte) 0x00, (byte) 0x11, (byte) 0x11, (byte) 0x00, (byte) 0x00, (byte) 0x30);

  static GUID IID_IDirectInput8W = new GUID(0xBF798031, (short) 0x483A, (short) 0x4DA2, (byte) 0xAA, (byte) 0x99, (byte) 0x5D, (byte) 0x64, (byte) 0xED, (byte) 0x36, (byte) 0x97, (byte) 0x00);

  static MethodHandle getLastError;

  static MethodHandle setupDiGetClassDevs;

  static MethodHandle setupDiEnumDeviceInfo;

  static MethodHandle setupDiEnumDeviceInterfaces;

  static MethodHandle setupDiGetDeviceInterfaceDetail;

  static MethodHandle setupDiGetDeviceRegistryProperty;

  static MethodHandle setupDiGetDeviceInstanceId;

  static MethodHandle directInput8Create;

  static MethodHandle getModuleHandle;

  static {
    getLastError = RuntimeHelper.downcallHandle("GetLastError",
            FunctionDescriptor.of(JAVA_INT));

    setupDiGetClassDevs = RuntimeHelper.downcallHandle("SetupDiGetClassDevsA",
            FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));

    setupDiEnumDeviceInfo = RuntimeHelper.downcallHandle("SetupDiEnumDeviceInfo",
            FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, JAVA_INT, ADDRESS));

    setupDiEnumDeviceInterfaces = RuntimeHelper.downcallHandle("SetupDiEnumDeviceInterfaces",
            FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, ADDRESS, ADDRESS, JAVA_INT, ADDRESS));

    setupDiGetDeviceInterfaceDetail = RuntimeHelper.downcallHandle("SetupDiGetDeviceInterfaceDetailA",
            FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT));

    setupDiGetDeviceRegistryProperty = RuntimeHelper.downcallHandle("SetupDiGetDeviceRegistryPropertyA",
            FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT));

    setupDiGetDeviceInstanceId = RuntimeHelper.downcallHandle("SetupDiGetDeviceInstanceIdA",
            FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));

    getModuleHandle = RuntimeHelper.downcallHandle("GetModuleHandleW",
            FunctionDescriptor.of(ADDRESS, ADDRESS));

    directInput8Create = RuntimeHelper.downcallHandle("DirectInput8Create",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
  }

  public static void main(String[] args) throws Throwable {
    // listSetupAPIDevices();
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
              .findStatic(Program.class, "enumDevicesCallback", MethodType.methodType(long.class, MemoryAddress.class, MemoryAddress.class));

      MemorySegment onEnumDevicesNativeSymbol = Linker.nativeLinker().upcallStub(
              onEnumDevices, FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS), memorySession);

      var enumDevices = IDirectInput8WVtbl.EnumDevices(directInput8.vtable, memorySession);

      int result = enumDevices.apply(directInput8.vtable, DI8DEVCLASS_GAMECTRL, onEnumDevicesNativeSymbol, MemoryAddress.NULL, DIEDFL_ALLDEVICES);
      if (result == DIERR_INVALIDPARAM) {
        System.out.println("DIERR_INVALIDPARAM: An invalid parameter was passed to the returning function, or the object was not in a state that permitted the function to be called.");
      }

      System.out.println("Found " + gamepads.size() + " gamepads.");
      for (var gamepad : gamepads) {
        System.out.println("\t" + gamepad.instanceName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static ArrayList<RawGamepad> gamepads = new ArrayList<>();

  public static long enumDevicesCallback(MemoryAddress lpddiSegment, MemoryAddress pvRef) {
    try (var memorySession = MemorySession.openConfined()) {
      var device = DIDEVICEINSTANCE.read(MemorySegment.ofAddress(lpddiSegment, DIDEVICEINSTANCE.$LAYOUT.byteSize(), memorySession));
      var name = new String(device.tszInstanceName).trim();
      var product = new String(device.tszProductName).trim();
      var type = DI8DEVTYPE.fromDwDevType(device.dwDevType);

      // for now we're only interested in gamepads, will add other types later
      if (type == DI8DEVTYPE.DI8DEVTYPE_GAMEPAD) {
        var gamepad = new RawGamepad(device.guidProduct.toUUID(), device.guidInstance.toUUID(), name, product);
        gamepads.add(gamepad);
      }else {
        System.out.println("found device that is not a gamepad: " + name + "[" + type + "]");
      }
    }
    return DIENUM_CONTINUE;
  }

  private static void listSetupAPIDevices() throws Throwable {
    try (var memorySession = MemorySession.openConfined()) {
      // prepare windows objects

      var devInterfaceHID = memorySession.allocate(GUID.$LAYOUT);
      GUID_DEVINTERFACE_HID.write(devInterfaceHID);

      SP_DEVINFO_DATA deviceInfoData = new SP_DEVINFO_DATA();
      var deviceInfoDataSegment = memorySession.allocate(SP_DEVINFO_DATA.$LAYOUT);
      deviceInfoData.write(deviceInfoDataSegment);

      SP_DEVICE_INTERFACE_DATA deviceInterfaceData = new SP_DEVICE_INTERFACE_DATA();
      var deviceInterfaceDataSegment = memorySession.allocate(SP_DEVICE_INTERFACE_DATA.$LAYOUT);
      deviceInterfaceData.write(deviceInterfaceDataSegment);

      // Get information for all the devices belonging to the HID class.
      var deviceInfoSet = SetupDiGetClassDevs(devInterfaceHID);
      if (deviceInfoSet == INVALID_HANDLE_VALUE) {
        System.out.println("Failed to create device enumerator.");
        return;
      }

      // Iterate over each device in the HID class
      for (int deviceIndex = 0; SetupDiEnumDeviceInfo(deviceInfoSet, deviceIndex, deviceInfoDataSegment); deviceIndex++) {

        var currentDeviceInfoData = SP_DEVINFO_DATA.read(deviceInfoDataSegment);

        System.out.println(currentDeviceInfoData.ClassGuid);

        int DEVICE_INSTANCE_ID_SIZE = 256;
        var deviceInstanceIdSegment = memorySession.allocate(MemoryLayout.sequenceLayout(DEVICE_INSTANCE_ID_SIZE, JAVA_BYTE));

        if (!SetupDiGetDeviceInstanceId(deviceInfoSet, deviceInfoDataSegment, deviceInstanceIdSegment, DEVICE_INSTANCE_ID_SIZE)) {
          reportLastError();
          continue;
        }

        var deviceId = deviceInstanceIdSegment.getUtf8String(0);
        System.out.println(deviceId);
      }

      if (deviceInfoData.ClassGuid == null) {
        reportLastError();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static long SetupDiGetClassDevs(Addressable ClassGuid) throws Throwable {
    Objects.requireNonNull(setupDiGetClassDevs);

    return (long) setupDiGetClassDevs.invoke(ClassGuid, MemoryAddress.NULL, 0, DIGCF_PRESENT | DIGCF_DEVICEINTERFACE);
  }

  private static boolean SetupDiEnumDeviceInfo(long DeviceInfoSet, int MemberIndex, Addressable DeviceInfoData) throws Throwable {
    Objects.requireNonNull(setupDiEnumDeviceInfo);
    return (boolean) setupDiEnumDeviceInfo.invoke(DeviceInfoSet, MemberIndex, DeviceInfoData);
  }

  private static boolean SetupDiEnumDeviceInterfaces(long DeviceInfoSet, Addressable DeviceInfoData, Addressable InterfaceClassGuid, int MemberIndex, Addressable DeviceInterfaceData) throws Throwable {
    Objects.requireNonNull(setupDiEnumDeviceInterfaces);
    return (boolean) setupDiEnumDeviceInterfaces.invoke(DeviceInfoSet, DeviceInfoData, InterfaceClassGuid, MemberIndex, DeviceInterfaceData);
  }

  private static boolean SetupDiGetDeviceRegistryProperty(long DeviceInfoSet, Addressable DeviceInfoData, int Property, Addressable PropertyBuffer, int PropertyBufferSize) throws Throwable {
    Objects.requireNonNull(setupDiGetDeviceRegistryProperty);
    return (boolean) setupDiGetDeviceRegistryProperty.invoke(DeviceInfoSet, DeviceInfoData, Property, 0, PropertyBuffer, PropertyBufferSize, PropertyBufferSize);
  }

  private static boolean SetupDiGetDeviceInstanceId(long DeviceInfoSet, Addressable DeviceInfoData, Addressable DeviceInstanceId, int DeviceInstanceIdSize) throws Throwable {
    Objects.requireNonNull(setupDiGetDeviceInstanceId);
    return (boolean) setupDiGetDeviceInstanceId.invoke(DeviceInfoSet, DeviceInfoData, DeviceInstanceId, DeviceInstanceIdSize, 0);
  }

  private static void reportLastError() throws Throwable {
    // https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-erref/18d8fbe8-a967-4f1c-ae50-99ca8e491d2d
    int errorMessageId = (int) getLastError.invoke();
    if (errorMessageId != 0) {
      StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
      String message = String.format("Win32 Error %s at %s:%d - https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-erref/18d8fbe8-a967-4f1c-ae50-99ca8e491d2d\n", String.format("0x%08X", errorMessageId), ste.getFileName(), ste.getLineNumber());
      throw new RuntimeException(message);
    }
  }
}
