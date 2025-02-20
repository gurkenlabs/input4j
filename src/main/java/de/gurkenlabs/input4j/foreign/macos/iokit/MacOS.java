package de.gurkenlabs.input4j.foreign.macos.iokit;

import jdk.jfr.MemoryAddress;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

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

  private static final MethodHandle CFRelease;

  private static final MethodHandle CFGetTypeID;
  private static final MethodHandle CFArrayGetTypeID;
  private static final MethodHandle CFDictionaryGetTypeID;
  private static final MethodHandle CFStringGetTypeID;
  private static final MethodHandle CFNumberGetTypeID;
  private static final MethodHandle CFNumberGetType;
  private static final MethodHandle CFNumberGetValue;
  private static final MethodHandle CFArrayGetCount;
  private static final MethodHandle CFArrayApplyFunction;

  private static final MethodHandle CFDictionaryGetCount;
  private static final MethodHandle CFDictionaryGetKeysAndValues;
  private static final MethodHandle CFDictionaryApplyFunction;

  private static final MethodHandle CFStringGetCString;
  private static final MethodHandle CFStringGetLength;
  private static final MethodHandle CFStringGetMaximumSizeForEncoding;


  private static final MethodHandle IOServiceMatching;
  private static final MethodHandle IOServiceGetMatchingServices;

  private static final MethodHandle IORegistryEntryCreateCFProperties;
  private static final MethodHandle IOIteratorNext;
  private static final MethodHandle IOCreatePlugInInterfaceForService;
  private static final MethodHandle IOObjectRelease;

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

  static {
    System.loadLibrary("CoreFoundation");
    System.loadLibrary("IOKit");

    CFRelease = downcallHandle("CFRelease", FunctionDescriptor.ofVoid(ADDRESS));

    CFGetTypeID = downcallHandle("CFGetTypeID", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFArrayGetTypeID = downcallHandle("CFArrayGetTypeID", FunctionDescriptor.of(JAVA_LONG));
    CFDictionaryGetTypeID = downcallHandle("CFDictionaryGetTypeID", FunctionDescriptor.of(JAVA_LONG));
    CFStringGetTypeID = downcallHandle("CFStringGetTypeID", FunctionDescriptor.of(JAVA_LONG));
    CFNumberGetTypeID = downcallHandle("CFNumberGetTypeID", FunctionDescriptor.of(JAVA_LONG));
    CFNumberGetType = downcallHandle("CFNumberGetType", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFNumberGetValue = downcallHandle("CFNumberGetValue", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, JAVA_INT, ADDRESS));
    CFArrayGetCount = downcallHandle("CFArrayGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFArrayApplyFunction = downcallHandle("CFArrayApplyFunction", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS));

    CFDictionaryGetCount = downcallHandle("CFDictionaryGetCount", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFDictionaryGetKeysAndValues = downcallHandle("CFDictionaryGetKeysAndValues", FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
    CFDictionaryApplyFunction = downcallHandle("CFDictionaryApplyFunction", FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS));

    CFStringGetLength = downcallHandle("CFStringGetLength", FunctionDescriptor.of(JAVA_INT, ADDRESS));
    CFStringGetMaximumSizeForEncoding = downcallHandle("CFStringGetMaximumSizeForEncoding", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT));
    CFStringGetCString = downcallHandle("CFStringGetCString", FunctionDescriptor.of(JAVA_BOOLEAN, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT));

    IOServiceMatching = downcallHandle("IOServiceMatching", FunctionDescriptor.of(ADDRESS, ADDRESS));
    IOServiceGetMatchingServices = downcallHandle("IOServiceGetMatchingServices", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS));
    IORegistryEntryCreateCFProperties = downcallHandle("IORegistryEntryCreateCFProperties", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT));
    IOIteratorNext = downcallHandle("IOIteratorNext", FunctionDescriptor.of(ADDRESS, ADDRESS));
    IOCreatePlugInInterfaceForService = downcallHandle("IOCreatePlugInInterfaceForService", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS));
    IOObjectRelease = downcallHandle("IOObjectRelease", FunctionDescriptor.of(JAVA_INT, ADDRESS));
  }

  /**
   * Releases a CoreFoundation object by decrementing its reference count.
   *
   * @param object The CoreFoundation object to release
   */
  public static void CFRelease(MemorySegment object) {
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
  public static int CFGetTypeID(MemorySegment obj) {
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
  public static long CFArrayGetTypeID() {
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
  public static long CFDictionaryGetTypeID() {
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
  public static long CFStringGetTypeID() {
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
  public static long CFNumberGetTypeID() {
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
  public static int CFNumberGetType(MemorySegment number) {
    try {
      return (int) CFNumberGetType.invoke(number);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Gets the value from a CFNumber object.
   *
   * @param number The CFNumber object
   * @param theType The type of number to retrieve
   * @param valuePtr Pointer to the memory where the value will be stored
   * @return true if successful, false otherwise
   */
  public static boolean CFNumberGetValue(MemorySegment number, int theType, MemorySegment valuePtr) {
    try {
      return (boolean) CFNumberGetValue.invoke(number, theType, valuePtr);
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
  public static int CFArrayGetCount(MemorySegment array) {
    try {
      return (int) CFArrayGetCount.invoke(array);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * Applies a callback function to each element in a CFArray.
   *
   * @param array The CFArray object
   * @param range The range of elements to process (can be null for all elements)
   * @param callback The callback function to apply
   * @param context User-defined context data (can be null)
   */
  public static void CFArrayApplyFunction(MemorySegment array, MemorySegment range,
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
   * @param callback The callback function to apply
   * @param context User-defined context data (can be NULL)
   */
  public static void CFDictionaryApplyFunction(MemorySegment dictionary, MemorySegment callback, MemorySegment context) {
    try {
      CFDictionaryApplyFunction.invoke(dictionary, callback, context);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public static String toString(Arena memoryArena, MemoryAddress cfString) {
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
  public static MemorySegment IOServiceMatching(Arena memoryArena) {
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
  public static int IOServiceGetMatchingServices(MemorySegment matching, MemorySegment iterator) {
    try {
      return (int) IOServiceGetMatchingServices.invoke(
              MemorySegment.NULL,  // masterPort, NULL = default
              matching,
              iterator);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
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
  public static MemorySegment IOIteratorNext(MemorySegment iterator) {
    try {
      return (MemorySegment) IOIteratorNext.invoke(iterator);
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
  public static int IOCreatePlugInInterfaceForService(MemorySegment service, MemorySegment theInterface, MemorySegment score) {
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
  public static int IOObjectRelease(MemorySegment object) {
    try {
      return (int) IOObjectRelease.invoke(object);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
