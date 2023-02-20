package de.gurkenlabs.litiengine.input.windows;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.ValueLayout.ADDRESS;

public class SetupApiPlayground {
  static int INVALID_HANDLE_VALUE = -1;
  static final int DIGCF_PRESENT = 2;
  static final int DIGCF_DEVICEINTERFACE = 16;
  static final int SPDRP_DEVICEDESC = 0x00000000;
  static final int SPDRP_CLASS = 0x00000007;
  static final int SPDRP_DRIVER = 0x00000009;

  static GUID GUID_DEVINTERFACE_HID = new GUID(0x4d1e55b2,
          (short) 0xf16f,
          (short) 0x11cf,
          (byte) 0x88, (byte) 0xcb, (byte) 0x00, (byte) 0x11, (byte) 0x11, (byte) 0x00, (byte) 0x00, (byte) 0x30);

  static MethodHandle getLastError;

  static MethodHandle setupDiGetClassDevs;

  static MethodHandle setupDiEnumDeviceInfo;

  static MethodHandle setupDiEnumDeviceInterfaces;

  static MethodHandle setupDiGetDeviceInterfaceDetail;

  static MethodHandle setupDiGetDeviceRegistryProperty;

  static MethodHandle setupDiGetDeviceInstanceId;

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
  }

  public static void main(String[] args) throws Throwable {
    listSetupAPIDevices();
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
