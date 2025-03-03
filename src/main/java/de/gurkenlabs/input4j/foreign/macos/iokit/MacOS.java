package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

class MacOS {
  private static final int kCFStringEncodingUTF8 = 0x08000100;
  private static final int kCFNumberIntType = 9;

  private final static String kIOHIDTransportKey = "Transport";
  private final static String kIOHIDVendorIDKey = "VendorID";
  private final static String kIOHIDProductIDKey = "ProductID";
  private final static String kIOHIDManufacturerKey = "Manufacturer";
  private final static String kIOHIDProductKey = "Product";
  private final static String kIOHIDPrimaryUsageKey = "PrimaryUsage";
  private final static String kIOHIDPrimaryUsagePageKey = "PrimaryUsagePage";

  private final static String kCFRunLoopDefaultMode = "kCFRunLoopDefaultMode";

  private static final MethodHandle CFRelease;
  private static final MethodHandle CFStringCreateWithCString;
  private static final MethodHandle CFNumberGetValue;
  private static final MethodHandle CFArrayGetCount;
  private static final MethodHandle CFArrayGetValueAtIndex;
  private static final MethodHandle CFSetGetCount;
  private static final MethodHandle CFSetGetValues;
  private static final MethodHandle CFStringGetCString;
  private static final MethodHandle CFRunLoopGetCurrent;
  private static final MethodHandle CFRunLoopRun;

  private static final MethodHandle IOHIDManagerCreate;
  private static final MethodHandle IOHIDManagerOpen;
  private static final MethodHandle IOHIDManagerCopyDevices;
  private static final MethodHandle IOHIDManagerSetDeviceMatching;
  private static final MethodHandle IOHIDManagerRegisterInputValueCallback;
  private static final MethodHandle IOHIDManagerScheduleWithRunLoop;
  private static final MethodHandle IOHIDManagerClose;

  private static final MethodHandle IOHIDDeviceGetProperty;
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

  static {
    System.load("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation");
    System.load("/System/Library/Frameworks/IOKit.framework/IOKit");

    // CoreFoundation methods
    CFRelease = downcallHandle("CFRelease", FunctionDescriptor.ofVoid(ADDRESS));
    CFStringCreateWithCString = downcallHandle("CFStringCreateWithCString", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, JAVA_INT));
    CFNumberGetValue = downcallHandle("CFNumberGetValue", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, JAVA_INT, ADDRESS));
    CFArrayGetCount = downcallHandle("CFArrayGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFArrayGetValueAtIndex = downcallHandle("CFArrayGetValueAtIndex", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));
    CFSetGetCount = downcallHandle("CFSetGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFSetGetValues = downcallHandle("CFSetGetValues", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    CFStringGetCString = downcallHandle("CFStringGetCString", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));
    CFRunLoopGetCurrent = downcallHandle("CFRunLoopGetCurrent", FunctionDescriptor.of(ADDRESS));
    CFRunLoopRun = downcallHandle("CFRunLoopRun", FunctionDescriptor.ofVoid());

    // IOKit methods
    IOHIDManagerCreate = downcallHandle("IOHIDManagerCreate", FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_INT));
    IOHIDManagerOpen = downcallHandle("IOHIDManagerOpen", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
    IOHIDManagerCopyDevices = downcallHandle("IOHIDManagerCopyDevices", FunctionDescriptor.of(ADDRESS, ADDRESS));
    IOHIDManagerSetDeviceMatching = downcallHandle("IOHIDManagerSetDeviceMatching", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS));
    IOHIDManagerRegisterInputValueCallback = downcallHandle("IOHIDManagerRegisterInputValueCallback", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
    IOHIDManagerScheduleWithRunLoop = downcallHandle("IOHIDManagerScheduleWithRunLoop", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
    IOHIDManagerClose = downcallHandle("IOHIDManagerClose", FunctionDescriptor.of(JAVA_INT, ADDRESS));

    IOHIDDeviceGetProperty = downcallHandle("IOHIDDeviceGetProperty", FunctionDescriptor.of(ADDRESS, JAVA_LONG, ADDRESS));
    IOHIDDeviceCopyMatchingElements = downcallHandle("IOHIDDeviceCopyMatchingElements", FunctionDescriptor.of(ADDRESS, JAVA_LONG, ADDRESS, JAVA_INT));

    IOHIDValueGetIntegerValue = downcallHandle("IOHIDValueGetIntegerValue", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    IOHIDValueGetElement = downcallHandle("IOHIDValueGetElement", FunctionDescriptor.of(ADDRESS, ADDRESS));
    IOHIDValueGetTimeStamp = downcallHandle("IOHIDValueGetTimeStamp", FunctionDescriptor.of(JAVA_LONG, ADDRESS));

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

  /**
   * Retrieves the IOHIDElement associated with the given IOHIDValue.
   *
   * @param ioHIDValue The IOHIDValue from which to retrieve the element.
   * @return The IOHIDElement associated with the given value.
   * @throws RuntimeException if an error occurs during the invocation.
   */
  static MemorySegment IOHIDValueGetElement(MemorySegment ioHIDValue) {
    try {
      return (MemorySegment) IOHIDValueGetElement.invoke(ioHIDValue);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Retrieves the integer value from the given IOHIDValue.
   *
   * @param ioHIDValue The IOHIDValue from which to retrieve the integer value.
   * @return The integer value associated with the given IOHIDValue.
   * @throws RuntimeException if an error occurs during the invocation.
   */
  static int IOHIDValueGetIntegerValue(MemorySegment ioHIDValue) {
    try {
      return (int) IOHIDValueGetIntegerValue.invoke(ioHIDValue);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Retrieves the timestamp from the given IOHIDValue.
   *
   * @param ioHIDValue The IOHIDValue from which to retrieve the timestamp.
   * @return The timestamp associated with the given IOHIDValue.
   * @throws RuntimeException if an error occurs during the invocation.
   */
  static long IOHIDValueGetTimeStamp(MemorySegment ioHIDValue) {
    try {
      return (long) IOHIDValueGetTimeStamp.invoke(ioHIDValue);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Initializes the HID manager and registers the input value callback.
   *
   * @param hidInputValueCallbackPointer The pointer to the input value callback function.
   * @return The initialized HID manager.
   * @throws RuntimeException if an error occurs during the initialization.
   */
  static MemorySegment initHIDManager(MemorySegment hidInputValueCallbackPointer) {
    try {
      var hidManager = (MemorySegment) IOHIDManagerCreate.invoke(MemorySegment.NULL, 0x00);

      var openResult = (int) IOHIDManagerOpen.invoke(hidManager, 0);
      if (openResult != IOReturn.kIOReturnSuccess) {
        throw new RuntimeException("Failed to open HID manager: " + openResult);
      }

      IOHIDManagerSetDeviceMatching.invoke(hidManager, MemorySegment.NULL);
      IOHIDManagerRegisterInputValueCallback.invoke(hidManager, hidInputValueCallbackPointer, MemorySegment.NULL);
      return hidManager;
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Closes the HID manager.
   *
   * @param hidManager The HID manager to close.
   * @return The result of the close operation.
   * @throws RuntimeException if an error occurs during the close operation.
   */
  static int IOHIDManagerClose(MemorySegment hidManager) {
    try {
      return (int) IOHIDManagerClose.invoke(hidManager);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Runs the event loop for the HID manager.
   *
   * @param memoryArena The memory arena to use for allocations.
   * @param hidManager  The HID manager for which to run the event loop.
   * @throws RuntimeException if an error occurs during the event loop execution.
   */
  static void runEventLoop(Arena memoryArena, MemorySegment hidManager) {
    try {
      var kCFRunLoopDefaultModeString = (MemorySegment) CFStringCreateWithCString.invoke(MemorySegment.NULL, memoryArena.allocateFrom(kCFRunLoopDefaultMode), kCFStringEncodingUTF8);
      try {
        IOHIDManagerScheduleWithRunLoop.invoke(hidManager, CFRunLoopGetCurrent.invoke(), kCFRunLoopDefaultModeString);
      } finally {
        CFRelease.invoke(kCFRunLoopDefaultModeString);
      }

      CFRunLoopRun.invoke();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Retrieves the supported HID devices from the HID manager.
   *
   * @param memoryArena The memory arena to use for allocations.
   * @param hidManager  The HID manager from which to retrieve the devices.
   * @return A collection of supported HID devices.
   * @throws RuntimeException if an error occurs during the retrieval.
   */
  static Collection<IOHIDDevice> getSupportedHIDDevices(Arena memoryArena, MemorySegment hidManager) {
    try {
      // Copy the set of devices from the HID manager
      var deviceSet = (MemorySegment) IOHIDManagerCopyDevices.invoke(hidManager);
      var count = (int) CFSetGetCount.invoke(deviceSet);
      if (deviceSet.equals(MemorySegment.NULL)) {
        throw new RuntimeException("Failed to copy devices from HID manager");
      }

      // Allocate memory for the devices
      var devices = memoryArena.allocate(JAVA_LONG, count);
      CFSetGetValues.invoke(deviceSet, devices);

      var hidDevices = new ArrayList<IOHIDDevice>();
      for (int i = 0; i < count; i++) {
        var device = new IOHIDDevice();
        device.address = devices.get(JAVA_LONG, i * JAVA_LONG.byteSize());

        // Initialize the device and check if it is supported
        if (!initializeDevice(memoryArena, device)) {
          continue;
        }

        // Copy matching elements for the device
        var elements = (MemorySegment) IOHIDDeviceCopyMatchingElements.invoke(device.address, MemorySegment.NULL, 0);
        if (!elements.equals(MemorySegment.NULL)) {
          var elementCount = (int) CFArrayGetCount.invoke(elements);
          for (int j = 0; j < elementCount; j++) {
            var elementAddress = (MemorySegment) CFArrayGetValueAtIndex.invoke(elements, j);
            if (elementAddress.equals(MemorySegment.NULL)) {
              continue;
            }

            // Get the IOHIDElement and add it to the device
            var element = getIOHIDElement(memoryArena, elementAddress);
            device.addElement(element);
          }
        }

        // Add the device to the list of HID devices
        hidDevices.add(device);
      }

      return hidDevices;
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private static boolean initializeDevice(Arena memoryArena, IOHIDDevice device) throws Throwable {
    device.vendorId = getIntProperty(memoryArena, kIOHIDVendorIDKey, device);
    device.productId = getIntProperty(memoryArena, kIOHIDProductIDKey, device);
    device.usage = getIntProperty(memoryArena, kIOHIDPrimaryUsageKey, device);
    device.usagePage = getIntProperty(memoryArena, kIOHIDPrimaryUsagePageKey, device);
    device.productName = getStringProperty(memoryArena, kIOHIDProductKey, device);
    device.manufacturer = getStringProperty(memoryArena, kIOHIDManufacturerKey, device);
    device.transport = getStringProperty(memoryArena, kIOHIDTransportKey, device);

    return device.isSupportedHIDDevice();
  }

  private static IOHIDElement getIOHIDElement(Arena memoryArena, MemorySegment elementAddress) throws Throwable {
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
    return element;
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
}
