package de.gurkenlabs.input4j.foreign.macos.iokit;

import jdk.jfr.MemoryAddress;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
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

  private final static String kIOHIDElementKey = "Elements";

  private final static String kIOHIDElementCookieKey = "ElementCookie";
  private final static String kIOHIDElementTypeKey = "Type";
  private final static String kIOHIDElementCollectionTypeKey = "CollectionType";
  private final static String kIOHIDElementUsageKey = "Usage";
  private final static String kIOHIDElementUsagePageKey = "UsagePage";
  private final static String kIOHIDElementMinKey = "Min";
  private final static String kIOHIDElementMaxKey = "Max";
  private final static String kIOHIDElementScaledMinKey = "ScaledMin";
  private final static String kIOHIDElementScaledMaxKey = "ScaledMax";
  private final static String kIOHIDElementSizeKey = "Size";
  private final static String kIOHIDElementReportSizeKey = "ReportSize";
  private final static String kIOHIDElementReportCountKey = "ReportCount";
  private final static String kIOHIDElementReportIDKey = "ReportID";
  private final static String kIOHIDElementIsArrayKey = "IsArray";
  private final static String kIOHIDElementIsRelativeKey = "IsRelative";
  private final static String kIOHIDElementIsWrappingKey = "IsWrapping";
  private final static String kIOHIDElementIsNonLinearKey = "IsNonLinear";
  private final static String kIOHIDElementHasPreferredStateKey = "HasPreferredState";
  private final static String kIOHIDElementHasNullStateKey = "HasNullState";
  private final static String kIOHIDElementUnitKey = "Unit";
  private final static String kIOHIDElementUnitExponentKey = "UnitExponent";
  private final static String kIOHIDElementNameKey = "Name";
  private final static String kIOHIDElementValueLocationKey = "ValueLocation";
  private final static String kIOHIDElementDuplicateIndexKey = "DuplicateIndex";
  private final static String kIOHIDElementParentCollectionKey = "ParentCollection";

  private static final MethodHandle CFRelease;
  private static final MethodHandle CFStringCreateWithCString;

  private static final MethodHandle CFGetTypeID;
  private static final MethodHandle CFArrayGetTypeID;
  private static final MethodHandle CFDictionaryGetTypeID;
  private static final MethodHandle CFStringGetTypeID;
  private static final MethodHandle CFNumberGetTypeID;
  private static final MethodHandle CFNumberGetType;
  private static final MethodHandle CFNumberGetValue;
  private static final MethodHandle CFArrayGetCount;
  private static final MethodHandle CFArrayApplyFunction;
  private static final MethodHandle CFArrayGetValueAtIndex;

  private static final MethodHandle CFDictionaryGetCount;
  private static final MethodHandle CFDictionaryGetKeysAndValues;
  private static final MethodHandle CFDictionaryApplyFunction;

  private static final MethodHandle CFSetGetCount;
  private static final MethodHandle CFSetGetValues;

  private static final MethodHandle CFStringGetCString;
  private static final MethodHandle CFStringGetLength;
  private static final MethodHandle CFStringGetMaximumSizeForEncoding;


  private static final MethodHandle IOServiceMatching;
  private static final MethodHandle IOServiceGetMatchingServices;

  private static final MethodHandle IORegistryEntryCreateCFProperties;
  private static final MethodHandle IOIteratorNext;
  private static final MethodHandle IOCreatePlugInInterfaceForService;
  private static final MethodHandle IOObjectRelease;

  private static final MethodHandle IOHIDManagerCreate;
  private static final MethodHandle IOHIDManagerOpen;
  private static final MethodHandle IOHIDManagerCopyDevices;
  private static final MethodHandle IOHIDManagerSetDeviceMatching;
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

  // Initialize UUID for HID Device User Client type
  // Corresponds to the constant kIOHIDDeviceUserClientTypeID from <IOKit/hid/IOHIDLib.h>
  // The bytes are arranged in UUID format: FA12FA38-6F1A-11D5-9F32-0005029B4E69.
  private static final MemorySegment kIOHIDDeviceUserClientTypeID = MemorySegment.ofArray(new byte[]{
          (byte) 0xFA, (byte) 0x12, (byte) 0xFA, (byte) 0x38,
          (byte) 0x6F, (byte) 0x1A,
          (byte) 0x11, (byte) 0xD5,
          (byte) 0x9F, (byte) 0x32,
          (byte) 0x00, (byte) 0x05,
          (byte) 0x02, (byte) 0x9B,
          (byte) 0x4E, (byte) 0x69
  });

  // Initialize UUID for CF Plugin Interface
  // Corresponds to the constant kIOCFPlugInInterfaceID from <IOKit/IOKitLib.h>
  // The bytes are arranged in UUID format: C244E858-109C-11D4-91D4-0050E4C6426F.
  private static final MemorySegment kIOCFPlugInInterfaceID = MemorySegment.ofArray(new byte[]{
          (byte) 0xC2, (byte) 0x44, (byte) 0xE8, (byte) 0x58,
          (byte) 0x10, (byte) 0x9C,
          (byte) 0x11, (byte) 0xD4,
          (byte) 0x91, (byte) 0xD4,
          (byte) 0x00, (byte) 0x50,
          (byte) 0xE4, (byte) 0xC6,
          (byte) 0x42, (byte) 0x6F
  });

  static final MemorySegment kIOHIDDeviceInterfaceID = MemorySegment.ofArray(new byte[]{
          (byte) 0x9A, (byte) 0x40, (byte) 0x4D, (byte) 0x7E,
          (byte) 0x9C, (byte) 0x7B,
          (byte) 0x11, (byte) 0xD4,
          (byte) 0x91, (byte) 0x4B,
          (byte) 0x00, (byte) 0x50,
          (byte) 0xE4, (byte) 0xC6,
          (byte) 0x42, (byte) 0x6F
  });

  static {
    System.load("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation");
    System.load("/System/Library/Frameworks/IOKit.framework/IOKit");

    CFRelease = downcallHandle("CFRelease", FunctionDescriptor.ofVoid(ADDRESS));
    CFStringCreateWithCString = downcallHandle("CFStringCreateWithCString", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, JAVA_INT));

    CFGetTypeID = downcallHandle("CFGetTypeID", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFArrayGetTypeID = downcallHandle("CFArrayGetTypeID", FunctionDescriptor.of(JAVA_LONG));

    CFDictionaryGetTypeID = downcallHandle("CFDictionaryGetTypeID", FunctionDescriptor.of(JAVA_LONG));
    CFStringGetTypeID = downcallHandle("CFStringGetTypeID", FunctionDescriptor.of(JAVA_LONG));
    CFNumberGetTypeID = downcallHandle("CFNumberGetTypeID", FunctionDescriptor.of(JAVA_LONG));
    CFNumberGetType = downcallHandle("CFNumberGetType", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFNumberGetValue = downcallHandle("CFNumberGetValue", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, JAVA_INT, ADDRESS));
    CFArrayGetCount = downcallHandle("CFArrayGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFArrayApplyFunction = downcallHandle("CFArrayApplyFunction", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS));
    CFArrayGetValueAtIndex = downcallHandle("CFArrayGetValueAtIndex", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));

    CFDictionaryGetCount = downcallHandle("CFDictionaryGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFDictionaryGetKeysAndValues = downcallHandle("CFDictionaryGetKeysAndValues", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
    CFDictionaryApplyFunction = downcallHandle("CFDictionaryApplyFunction", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS));
    CFSetGetCount = downcallHandle("CFSetGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFSetGetValues = downcallHandle("CFSetGetValues", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));

    CFStringGetLength = downcallHandle("CFStringGetLength", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFStringGetMaximumSizeForEncoding = downcallHandle("CFStringGetMaximumSizeForEncoding", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT));
    CFStringGetCString = downcallHandle("CFStringGetCString", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));

    IOServiceMatching = downcallHandle("IOServiceMatching", FunctionDescriptor.of(ADDRESS, ADDRESS));
    IOServiceGetMatchingServices = downcallHandle("IOServiceGetMatchingServices", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
    IORegistryEntryCreateCFProperties = downcallHandle("IORegistryEntryCreateCFProperties", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT));
    IOIteratorNext = downcallHandle("IOIteratorNext", FunctionDescriptor.of(JAVA_LONG, JAVA_LONG));
    IOCreatePlugInInterfaceForService = downcallHandle("IOCreatePlugInInterfaceForService", FunctionDescriptor.of(JAVA_INT, JAVA_LONG, ADDRESS, ADDRESS, ADDRESS, ADDRESS));
    IOObjectRelease = downcallHandle("IOObjectRelease", FunctionDescriptor.of(JAVA_INT, JAVA_LONG));

    IOHIDManagerCreate = downcallHandle("IOHIDManagerCreate", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));
    IOHIDManagerOpen = downcallHandle("IOHIDManagerOpen", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
    IOHIDManagerCopyDevices = downcallHandle("IOHIDManagerCopyDevices", FunctionDescriptor.of(ADDRESS, ADDRESS));
    IOHIDManagerSetDeviceMatching = downcallHandle("IOHIDManagerSetDeviceMatching", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
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

  /**
   * Releases a CoreFoundation object by decrementing its reference count.
   *
   * @param object The CoreFoundation object to release
   */
  static void CFRelease(MemorySegment object) {
    try {
      CFRelease.invoke(object);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns the type identifier of a CoreFoundation object.
   *
   * @param obj The CoreFoundation object
   * @return The type ID of the object
   */
  static int CFGetTypeID(MemorySegment obj) {
    try {
      return (int) CFGetTypeID.invoke(obj);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns the type identifier for the CFArray type.
   *
   * @return The type ID for CFArray
   */
  static long CFArrayGetTypeID() {
    try {
      return (long) CFArrayGetTypeID.invoke();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns the type identifier for the CFDictionary type.
   *
   * @return The type ID for CFDictionary
   */
  static long CFDictionaryGetTypeID() {
    try {
      return (long) CFDictionaryGetTypeID.invoke();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns the type identifier for the CFString type.
   *
   * @return The type ID for CFString
   */
  static long CFStringGetTypeID() {
    try {
      return (long) CFStringGetTypeID.invoke();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns the type identifier for the CFNumber type.
   *
   * @return The type ID for CFNumber
   */
  static long CFNumberGetTypeID() {
    try {
      return (long) CFNumberGetTypeID.invoke();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns the type of the specified CFNumber.
   *
   * @param number The CFNumber object
   * @return The CFNumberType value indicating the type
   */
  static int CFNumberGetType(MemorySegment number) {
    try {
      return (int) CFNumberGetType.invoke(number);
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

  /**
   * Applies a callback function to each element in a CFArray.
   *
   * @param array    The CFArray object
   * @param range    The range of elements to process (can be null for all elements)
   * @param callback The callback function to apply
   * @param context  User-defined context data (can be null)
   */
  static void CFArrayApplyFunction(MemorySegment array, MemorySegment range,
                                   MemorySegment callback, MemorySegment context) {
    try {
      CFArrayApplyFunction.invoke(array, range, callback, context);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Applies a callback function to each key-value pair in a dictionary.
   *
   * @param dictionary The dictionary to iterate over
   * @param callback   The callback function to apply
   * @param context    User-defined context data (can be NULL)
   */
  static void CFDictionaryApplyFunction(MemorySegment dictionary, MemorySegment callback, MemorySegment context) {
    try {
      CFDictionaryApplyFunction.invoke(dictionary, callback, context);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  static String toString(Arena memoryArena, MemoryAddress cfString) {
    try {
      int unicodeLength = (int) CFStringGetLength.invoke(cfString);
      int utf8Length = (int) CFStringGetMaximumSizeForEncoding.invoke(unicodeLength, kCFStringEncodingUTF8);

      MemorySegment buffer = memoryArena.allocate(utf8Length + 1);
      boolean result = (boolean) CFStringGetCString.invoke(cfString, buffer.address(), utf8Length + 1, kCFStringEncodingUTF8);

      if (!result) {
        return null;
      }

      return buffer.getString(0, StandardCharsets.UTF_8);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Creates a matching dictionary for IOKit services by class name.
   *
   * @return A CFMutableDictionaryRef containing the matching criteria
   */
  static MemorySegment IOServiceMatching(Arena memoryArena) {
    try {
      var nameSegment = memoryArena.allocateFrom(kIOHIDDeviceKey);
      return (MemorySegment) IOServiceMatching.invoke(nameSegment);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns an iterator containing IOKit services that match a matching dictionary.
   *
   * @param matching The matching criteria dictionary (will be consumed)
   * @param iterator Address to store the created iterator
   * @return IOReturn status code
   */
  static int IOServiceGetMatchingServices(MemorySegment matching, MemorySegment iterator) {
    try {
      return (int) IOServiceGetMatchingServices.invoke(
              MemorySegment.NULL,  // masterPort, NULL = default
              matching,
              iterator);
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
      if (!hidManager.equals(MemorySegment.NULL)) {
        IOObjectRelease(hidManager.address());
      }

      if (!deviceSet.equals(MemorySegment.NULL)) {
        IOObjectRelease(deviceSet.address());
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
   * Creates a CFDictionary containing the properties of an I/O Registry entry.
   *
   * @param entry      The registry entry to retrieve properties from
   * @param properties Address to store the created properties dictionary
   * @return IOReturn status code
   */
  public static int IORegistryEntryCreateCFProperties(MemorySegment entry, MemorySegment properties) {
    try {
      return (int) IORegistryEntryCreateCFProperties.invoke(
              entry,
              properties,
              MemorySegment.NULL,  // allocator, NULL = default
              0                    // options, typically 0
      );
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Returns the next object in an I/O Registry iterator.
   *
   * @param iterator The I/O Registry iterator
   * @return The next object in the iterator, or NULL if there are no more objects
   */
  public static long IOIteratorNext(long iterator) {
    try {
      return (long) IOIteratorNext.invoke(iterator);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Creates a plug-in interface for an IOKit service.
   *
   * @param service      The IOKit service to create the interface for
   * @param theInterface Address to store the created interface
   * @param score        Address to store the scoring result (can be NULL)
   * @return IOReturn status code
   */
  public static int IOCreatePlugInInterfaceForService(long service, MemorySegment theInterface, MemorySegment score) {
    try {
      return (int) IOCreatePlugInInterfaceForService.invoke(
              service,
              kIOHIDDeviceUserClientTypeID,
              kIOCFPlugInInterfaceID,
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
