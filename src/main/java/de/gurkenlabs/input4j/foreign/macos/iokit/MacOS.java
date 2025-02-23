package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

public class MacOS {
  static final int kCFStringEncodingUTF8 = 0x08000100;
  // CFNumber types
  public static final int kCFNumberSInt8Type = 1;
  public static final int kCFNumberSInt16Type = 2;
  public static final int kCFNumberSInt32Type = 3;
  public static final int kCFNumberSInt64Type = 4;
  public static final int kCFNumberFloat32Type = 5;
  public static final int kCFNumberFloat64Type = 6;
  public static final int kCFNumberCharType = 7;
  public static final int kCFNumberShortType = 8;
  public static final int kCFNumberIntType = 9;
  public static final int kCFNumberLongType = 10;
  public static final int kCFNumberLongLongType = 11;
  public static final int kCFNumberFloatType = 12;
  public static final int kCFNumberDoubleType = 13;
  public static final int kCFNumberCFIndexType = 14;

  private final static String kIOHIDTransportKey = "Transport";
  private final static String kIOHIDVendorIDKey = "VendorID";
  private final static String kIOHIDVendorIDSourceKey = "VendorIDSource";
  private final static String kIOHIDProductIDKey = "ProductID";
  private final static String kIOHIDVersionNumberKey = "VersionNumber";
  private final static String kIOHIDManufacturerKey = "Manufacturer";
  private final static String kIOHIDProductKey = "Product";
  private final static String kIOHIDSerialNumberKey = "SerialNumber";
  private final static String kIOHIDCountryCodeKey = "CountryCode";
  private final static String kIOHIDLocationIDKey = "LocationID";
  private final static String kIOHIDDeviceUsageKey = "DeviceUsage";
  private final static String kIOHIDDeviceUsagePageKey = "DeviceUsagePage";
  private final static String kIOHIDDeviceUsagePairsKey = "DeviceUsagePairs";
  private final static String kIOHIDPrimaryUsageKey = "PrimaryUsage";
  private final static String kIOHIDPrimaryUsagePageKey = "PrimaryUsagePage";
  private final static String kIOHIDMaxInputReportSizeKey = "MaxInputReportSize";
  private final static String kIOHIDMaxOutputReportSizeKey = "MaxOutputReportSize";
  private final static String kIOHIDMaxFeatureReportSizeKey = "MaxFeatureReportSize";

  private static final MethodHandle CFRelease;
  private static final MethodHandle CFStringCreateWithCString;
  private static final MethodHandle CFNumberGetValue;
  private static final MethodHandle CFArrayGetCount;
  private static final MethodHandle CFArrayGetValueAtIndex;
  private static final MethodHandle CFUUIDGetConstantUUIDWithBytes;
  private static final MethodHandle CFSetGetCount;
  private static final MethodHandle CFSetGetValues;
  private static final MethodHandle CFStringGetCString;

  private static final MethodHandle IOCreatePlugInInterfaceForService;
  private static final MethodHandle IOObjectRelease;

  private static final MethodHandle IOHIDManagerCreate;
  private static final MethodHandle IOHIDManagerOpen;
  private static final MethodHandle IOHIDManagerCopyDevices;
  private static final MethodHandle IOHIDManagerSetDeviceMatching;
  private static final MethodHandle IOHIDManagerRegisterInputValueCallback;

  private static final MethodHandle IOHIDDeviceGetService;
  private static final MethodHandle IOHIDDeviceGetProperty;
  private static final MethodHandle IOHIDDeviceGetValue;
  private static final MethodHandle IOHIDDeviceCopyMatchingElements;

  private static final MethodHandle IOHIDValueGetIntegerValue;
  private static final MethodHandle IOHIDValueGetElement;
  private static final MethodHandle IOHIDValueGetTimeStamp;

  private static final MethodHandle IOHIDElementGetName;
  private static final MethodHandle IOHIDElementGetUsage;
  private static final MethodHandle IOHIDElementGetUsagePage;
  private static final MethodHandle IOHIDElementGetType;
  private static final MethodHandle IOHIDElementGetLogicalMin;
  private static final MethodHandle IOHIDElementGetLogicalMax;
  private static final MethodHandle IOHIDElementGetPhysicalMin;
  private static final MethodHandle IOHIDElementGetPhysicalMax;
  private static final MethodHandle IOHIDElementGetUnit;
  private static final MethodHandle IOHIDElementGetUnitExponent;
  private static final MethodHandle IOHIDElementGetReportSize;

  /**
   * Key for matching HID devices in the IOKit registry.
   */
  private static final String kIOHIDDeviceKey = "IOHIDDevice";

  static {
    System.load("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation");
    System.load("/System/Library/Frameworks/IOKit.framework/IOKit");

    CFRelease = downcallHandle("CFRelease", FunctionDescriptor.ofVoid(ADDRESS));
    CFStringCreateWithCString = downcallHandle("CFStringCreateWithCString", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, JAVA_INT));
    CFNumberGetValue = downcallHandle("CFNumberGetValue", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, JAVA_INT, ADDRESS));
    CFArrayGetCount = downcallHandle("CFArrayGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFArrayGetValueAtIndex = downcallHandle("CFArrayGetValueAtIndex", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));
    CFUUIDGetConstantUUIDWithBytes = downcallHandle("CFUUIDGetConstantUUIDWithBytes", FunctionDescriptor.of(ADDRESS, ADDRESS,
            JAVA_BYTE, JAVA_BYTE, JAVA_BYTE, JAVA_BYTE,
            JAVA_BYTE, JAVA_BYTE, JAVA_BYTE, JAVA_BYTE,
            JAVA_BYTE, JAVA_BYTE, JAVA_BYTE, JAVA_BYTE,
            JAVA_BYTE, JAVA_BYTE, JAVA_BYTE, JAVA_BYTE));

    CFSetGetCount = downcallHandle("CFSetGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFSetGetValues = downcallHandle("CFSetGetValues", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    CFStringGetCString = downcallHandle("CFStringGetCString", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));
    IOCreatePlugInInterfaceForService = downcallHandle("IOCreatePlugInInterfaceForService", FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS));
    IOObjectRelease = downcallHandle("IOObjectRelease", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));

    IOHIDManagerCreate = downcallHandle("IOHIDManagerCreate", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));
    IOHIDManagerOpen = downcallHandle("IOHIDManagerOpen", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
    IOHIDManagerCopyDevices = downcallHandle("IOHIDManagerCopyDevices", FunctionDescriptor.of(ADDRESS, ADDRESS));
    IOHIDManagerSetDeviceMatching = downcallHandle("IOHIDManagerSetDeviceMatching", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    IOHIDManagerRegisterInputValueCallback = downcallHandle("IOHIDManagerRegisterInputValueCallback", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS));

    IOHIDDeviceGetService = downcallHandle("IOHIDDeviceGetService", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDDeviceGetProperty = downcallHandle("IOHIDDeviceGetProperty", FunctionDescriptor.of(ADDRESS, JAVA_LONG, ADDRESS));
    IOHIDDeviceCopyMatchingElements = downcallHandle("IOHIDDeviceCopyMatchingElements", FunctionDescriptor.of(ADDRESS, JAVA_LONG, ADDRESS, JAVA_INT));
    IOHIDDeviceGetValue = downcallHandle("IOHIDDeviceGetValue", FunctionDescriptor.of(JAVA_INT, JAVA_LONG, JAVA_LONG, ADDRESS));

    IOHIDValueGetIntegerValue = downcallHandle("IOHIDValueGetIntegerValue", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    IOHIDValueGetElement = downcallHandle("IOHIDValueGetElement", FunctionDescriptor.of(ADDRESS, ADDRESS));
    IOHIDValueGetTimeStamp = downcallHandle("IOHIDValueGetTimeStamp", FunctionDescriptor.of(ADDRESS, ADDRESS));

    IOHIDElementGetName = downcallHandle("IOHIDElementGetName", FunctionDescriptor.of(ADDRESS, JAVA_LONG));
    IOHIDElementGetUsage = downcallHandle("IOHIDElementGetUsage", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetUsagePage = downcallHandle("IOHIDElementGetUsagePage", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetType = downcallHandle("IOHIDElementGetType", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetLogicalMin = downcallHandle("IOHIDElementGetLogicalMin", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetLogicalMax = downcallHandle("IOHIDElementGetLogicalMax", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetPhysicalMin = downcallHandle("IOHIDElementGetPhysicalMin", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetPhysicalMax = downcallHandle("IOHIDElementGetPhysicalMax", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetUnit = downcallHandle("IOHIDElementGetUnit", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetUnitExponent = downcallHandle("IOHIDElementGetUnitExponent", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
    IOHIDElementGetReportSize = downcallHandle("IOHIDElementGetReportSize", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));
  }

  public static void hidInputValueCallback(MemorySegment context, int result, MemorySegment sender, MemorySegment ioHIDValueRef) {
    System.out.println("hidInputValueCallback");
  }

  private static MemorySegment hidInputValueCallbackPointer(Arena memoryArena) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle enumDeviceMethodHandle = MethodHandles.lookup()
            .findStatic(MacOS.class, "hidInputValueCallback", MethodType.methodType(void.class, MemorySegment.class, int.class, MemorySegment.class, MemorySegment.class));

    return Linker.nativeLinker().upcallStub(
            enumDeviceMethodHandle, FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS, ADDRESS), memoryArena);
  }

  static int IOHIDDeviceGetValue(IOHIDDevice device, IOHIDElement element, MemorySegment value) {
    try {
      return (int) IOHIDDeviceGetValue.invoke(device.address, element.address, value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static MemorySegment IOHIDValueGetElement(MemorySegment value) {
    try {
      return (MemorySegment) IOHIDValueGetElement.invoke(value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static int IOHIDValueGetIntegerValue(MemorySegment value) {
    try {
      return (int) IOHIDValueGetIntegerValue.invoke(value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static long IOHIDValueGetTimeStamp(MemorySegment value) {
    try {
      return (long) IOHIDValueGetTimeStamp.invoke(value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static int IOHIDDeviceGetService(IOHIDDevice device) {
    try {
      return (int) IOHIDDeviceGetService.invoke(device.address);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static MemorySegment getIOHIDDeviceUserClientTypeID() {
    try {
      // 6C29DD01-7C99-11D6-9D86-0003933E3E3E
      return (MemorySegment) CFUUIDGetConstantUUIDWithBytes.invoke(
              MemorySegment.NULL,
              (byte) 0x6C, (byte) 0x29, (byte) 0xDD, (byte) 0x01,
              (byte) 0x7C, (byte) 0x99, (byte) 0x11, (byte) 0xD6,
              (byte) 0x9D, (byte) 0x86, (byte) 0x00, (byte) 0x03,
              (byte) 0x93, (byte) 0x3E, (byte) 0x3E, (byte) 0x3E
      );
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static MemorySegment getIOCFPlugInInterfaceID() {
    try {
      // C2264A11-3B06-11D4-BD62-000502C34D39
      return (MemorySegment) CFUUIDGetConstantUUIDWithBytes.invoke(
              MemorySegment.NULL,
              (byte) 0xC2, (byte) 0x26, (byte) 0x4A, (byte) 0x11,
              (byte) 0x3B, (byte) 0x06, (byte) 0x11, (byte) 0xD4,
              (byte) 0xBD, (byte) 0x62, (byte) 0x00, (byte) 0x05,
              (byte) 0x02, (byte) 0xC3, (byte) 0x4D, (byte) 0x39
      );
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns the number of values in a CFArray.
   *
   * @param array The CFArray object
   * @return The number of values in the array
   */
  static int CFArrayGetCount(MemorySegment array) {
    try {
      return (int) CFArrayGetCount.invoke(array);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static Collection<IOHIDDevice> getSupportedHIDDevices(Arena memoryArena) {
    var hidManager = MemorySegment.NULL;
    var deviceSet = MemorySegment.NULL;
    try {
      hidManager = (MemorySegment) IOHIDManagerCreate.invoke(MemorySegment.NULL, 0x00);

      var openResult = (int) IOHIDManagerOpen.invoke(hidManager, 0);
      if (openResult != IOReturn.kIOReturnSuccess) {
        throw new RuntimeException("Failed to open HID manager: " + openResult);
      }

      IOHIDManagerSetDeviceMatching.invoke(hidManager, MemorySegment.NULL);

      IOHIDManagerRegisterInputValueCallback.invoke(hidManager, hidInputValueCallbackPointer(memoryArena), MemorySegment.NULL);

      deviceSet = (MemorySegment) IOHIDManagerCopyDevices.invoke(hidManager);
      var count = (int) CFSetGetCount.invoke(deviceSet);
      System.out.println("Found a total" + count + " HID devices");
      if (deviceSet.equals(MemorySegment.NULL)) {
        throw new RuntimeException("Failed to copy devices from HID manager");
      }

      var devices = memoryArena.allocate(JAVA_LONG, count);
      CFSetGetValues.invoke(deviceSet, devices);

      var hidDevices = new ArrayList<IOHIDDevice>();
      for (int i = 0; i < count; i++) {
        var device = new IOHIDDevice();
        device.address = devices.get(JAVA_LONG, i * JAVA_LONG.byteSize());

        device.vendorId = getIntProperty(memoryArena, kIOHIDVendorIDKey, device);
        device.productId = getIntProperty(memoryArena, kIOHIDProductIDKey, device);
        device.usage = getIntProperty(memoryArena, kIOHIDPrimaryUsageKey, device);
        device.usagePage = getIntProperty(memoryArena, kIOHIDPrimaryUsagePageKey, device);
        device.productName = getStringProperty(memoryArena, kIOHIDProductKey, device);
        device.manufacturer = getStringProperty(memoryArena, kIOHIDManufacturerKey, device);
        device.transport = getStringProperty(memoryArena, kIOHIDTransportKey, device);

        if (!device.isSupportedHIDDevice()) {
          continue;
        }

        System.out.println("Device " + i + ": " + device);

        var elements = (MemorySegment) IOHIDDeviceCopyMatchingElements.invoke(device.address, MemorySegment.NULL, 0);
        if (!elements.equals(MemorySegment.NULL)) {
          var elementCount = CFArrayGetCount(elements);
          for (int j = 0; j < elementCount; j++) {
            var elementAddress = (MemorySegment) CFArrayGetValueAtIndex.invoke(elements, j);
            if (elementAddress.equals(MemorySegment.NULL)) {
              continue;
            }

            var element = new IOHIDElement();
            element.address = elementAddress.address();

            var elementNameRef = (MemorySegment) IOHIDElementGetName.invoke(element.address);
            if (!elementNameRef.equals(MemorySegment.NULL)) {
              var elementNameSegment = memoryArena.allocate(JAVA_CHAR, 256);
              if ((boolean) CFStringGetCString.invoke(elementNameRef, elementNameSegment, 256, kCFStringEncodingUTF8) && elementNameSegment != MemorySegment.NULL) {
                element.name = elementNameSegment.reinterpret(256).getString(0, StandardCharsets.UTF_8);
              }
            }

            element.type = IOHIDElementType.fromValue((int) IOHIDElementGetType.invoke(element.address));
            element.usagePage = IOHIDElementUsagePage.fromValue((int) IOHIDElementGetUsagePage.invoke(element.address));
            element.usage = IOHIDElementUsage.fromValue((int) IOHIDElementGetUsage.invoke(element.address));
            element.min = (int) IOHIDElementGetLogicalMin.invoke(element.address);
            element.max = (int) IOHIDElementGetLogicalMax.invoke(element.address);
            element.physicalMin = (int) IOHIDElementGetPhysicalMin.invoke(element.address);
            element.physicalMax = (int) IOHIDElementGetPhysicalMax.invoke(element.address);
            element.unit = (int) IOHIDElementGetUnit.invoke(element.address);
            element.unitExponent = (int) IOHIDElementGetUnitExponent.invoke(element.address);
            element.reportSize = (int) IOHIDElementGetReportSize.invoke(element.address);
            device.addElement(element);
            System.out.println("Element " + j + ": " + element);
          }
        }
        hidDevices.add(device);
      }

      return hidDevices;
    } catch (Throwable t) {
      throw new RuntimeException(t);
    } finally {
      // TODO: don't release too early otherwise we cannot use the devices later. But still release at some point
      // use IOHIDManagerClose instead of manually releasing the object
      if (!hidManager.equals(MemorySegment.NULL)) {
       // IOObjectRelease(hidManager.address());
      }

      if (!deviceSet.equals(MemorySegment.NULL)) {
       // IOObjectRelease(deviceSet.address());
      }
    }
  }

  private static int getIntProperty(Arena memoryArena, String propertyKey, IOHIDDevice device) throws Throwable {
    var propertyKeyString = (MemorySegment) CFStringCreateWithCString.invoke(MemorySegment.NULL, memoryArena.allocateFrom(propertyKey), kCFStringEncodingUTF8);
    try {
      var propertyRef = (MemorySegment) IOHIDDeviceGetProperty.invoke(device.address, propertyKeyString);
      if (!propertyRef.equals(MemorySegment.NULL)) {
        var propertyValue = memoryArena.allocate(JAVA_INT);
        if ((boolean) CFNumberGetValue.invoke(propertyRef, kCFNumberIntType, propertyValue)) {
          return propertyValue.get(JAVA_INT, 0);
        }
      }
    } finally {
      CFRelease.invoke(propertyKeyString);
    }

    return 0;
  }

  private static String getStringProperty(Arena memoryArena, String propertyKey, IOHIDDevice device) throws Throwable {
    final int MAX_STRING_LENGTH = 256;
    var propertyKeyString = (MemorySegment) CFStringCreateWithCString.invoke(MemorySegment.NULL, memoryArena.allocateFrom(propertyKey), kCFStringEncodingUTF8);
    try {
      var propertyRef = (MemorySegment) IOHIDDeviceGetProperty.invoke(device.address, propertyKeyString);
      if (!propertyRef.equals(MemorySegment.NULL)) {
        var propertyValue = memoryArena.allocate(JAVA_CHAR, MAX_STRING_LENGTH);
        if ((boolean) CFStringGetCString.invoke(propertyRef, propertyValue, MAX_STRING_LENGTH, kCFStringEncodingUTF8) && propertyValue != MemorySegment.NULL) {
          return propertyValue.reinterpret(MAX_STRING_LENGTH).getString(0, StandardCharsets.UTF_8);
        }
      }
    } finally {
      CFRelease.invoke(propertyKeyString);
    }

    return null;
  }

  /**
   * Creates a plug-in interface for an IOKit service.
   *
   * @param service      The IOKit service to create the interface for
   * @param theInterface Address to store the created interface
   * @param score        Address to store the scoring result (can be NULL)
   * @return IOReturn status code
   */
  public static int IOCreatePlugInInterfaceForService(Arena memorySegment, int service, MemorySegment theInterface, MemorySegment score) {
    try {
      return (int) IOCreatePlugInInterfaceForService.invoke(
              service,
              getIOHIDDeviceUserClientTypeID(),
              getIOCFPlugInInterfaceID(),
              theInterface,
              score);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Releases a reference to an IOKit object.
   *
   * @param object The IOKit object to release
   * @return IOReturn status code
   */
  public static int IOObjectRelease(long object) {
    try {
      return (int) IOObjectRelease.invoke(object);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
