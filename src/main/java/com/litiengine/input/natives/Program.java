package com.litiengine.input.natives;


import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

import static java.lang.foreign.ValueLayout.*;

public class Program {
  public static int INVALID_HANDLE_VALUE = -1;
  public static final int DIGCF_PRESENT = 2;

  public static final int DIGCF_DEVICEINTERFACE = 16;

  static Linker linker;
  static SymbolLookup linkerLookup;

  static SymbolLookup systemLookup;

  static SymbolLookup symbolLookup;

  static MethodHandle getLastError;

  static MethodHandle setupDiGetClassDevs;

  static MethodHandle setupDiEnumDeviceInfo;

  static {

    System.loadLibrary("setupapi");
    System.loadLibrary("Kernel32");

    linker = Linker.nativeLinker();
    linkerLookup = linker.defaultLookup();
    systemLookup = SymbolLookup.loaderLookup();
    symbolLookup = name -> systemLookup.lookup(name).or(() -> linkerLookup.lookup(name));

    var getLastErrorDescriptor = FunctionDescriptor.of(JAVA_INT);
    getLastError = symbolLookup.lookup("GetLastError")
            .map(addr -> linker.downcallHandle(addr, getLastErrorDescriptor)).orElse(null);

    var setupDiGetClassDevsDescriptor = FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT);
    setupDiGetClassDevs = symbolLookup.lookup("SetupDiGetClassDevsA")
            .map(addr -> linker.downcallHandle(addr, setupDiGetClassDevsDescriptor)).orElse(null);

    var setupDiEnumDeviceInfoDescriptor = FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_LONG, JAVA_INT, ADDRESS);
    setupDiEnumDeviceInfo = symbolLookup.lookup("SetupDiEnumDeviceInfo")
            .map(addr -> linker.downcallHandle(addr, setupDiEnumDeviceInfoDescriptor)).orElse(null);
  }

  public static void main(String[] args) throws Throwable {
    try (var memorySession = MemorySession.openConfined()) {
      // prepare windows objects
      SP_DEVINFO_DATA deviceInfoData = new SP_DEVINFO_DATA();
      var deviceInfoDataSegment = memorySession.allocate(SP_DEVINFO_DATA.$LAYOUT);
      deviceInfoData.write(deviceInfoDataSegment);

      // Get information for all the devices belonging to the HID class.
      var deviceInfoSet = SetupDiGetClassDevs(memorySession);
      if (deviceInfoSet == INVALID_HANDLE_VALUE) {
        System.out.println("Failed to create device enumerator.");
        return;
      }
      System.out.println(deviceInfoSet);

      // Iterate over each device in the HID class
      for (int deviceIndex = 0; SetupDiEnumDeviceInfo(deviceInfoSet, deviceIndex, deviceInfoDataSegment); deviceIndex++) {
        var currentDeviceInfoData = SP_DEVINFO_DATA.read(deviceInfoDataSegment);

        System.out.println(currentDeviceInfoData.ClassGuid);
      }

      if (deviceInfoData.ClassGuid == null) {
        reportLastError();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static long SetupDiGetClassDevs(MemorySession memorySession) throws Throwable {
    Objects.requireNonNull(setupDiGetClassDevs);
    var GUID_DEVINTERFACE_HID = new GUID(0x4d1e55b2,
            (short) 0xf16f,
            (short) 0x11cf,
            (byte) 0x88, (byte) 0xcb, (byte) 0x00, (byte) 0x11, (byte) 0x11, (byte) 0x00, (byte) 0x00, (byte) 0x30);

    var guidSegment = memorySession.allocate(GUID.$LAYOUT);
    GUID_DEVINTERFACE_HID.write(guidSegment);

    return (long) setupDiGetClassDevs.invoke(guidSegment.address(), memorySession.allocateUtf8String(""), 0, DIGCF_PRESENT | DIGCF_DEVICEINTERFACE);
  }

  private static boolean SetupDiEnumDeviceInfo(long DeviceInfoSet, int MemberIndex, Addressable DeviceInfoData) throws Throwable {
    Objects.requireNonNull(setupDiEnumDeviceInfo);
    return (boolean) setupDiEnumDeviceInfo.invoke(DeviceInfoSet, MemberIndex, DeviceInfoData);
  }

  private static void reportLastError() throws Throwable {
    // https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-erref/18d8fbe8-a967-4f1c-ae50-99ca8e491d2d
    int errorMessageId = (int) getLastError.invoke();
    if (errorMessageId != 0) {
      StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
      String message = String.format("Win32 Error %s at %s:%d\n", String.format("0x%08X", errorMessageId), ste.getFileName(), ste.getLineNumber());
      throw new RuntimeException(message);
    }
  }
}
