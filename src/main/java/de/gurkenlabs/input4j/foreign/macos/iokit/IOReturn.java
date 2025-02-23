package de.gurkenlabs.input4j.foreign.macos.iokit;

/**
 * The `IOReturn` class defines constants for various IOKit return codes.
 * These constants represent different result codes that can be returned by IOKit functions.
 */
class IOReturn {
  static final int kIOReturnSuccess = 0;
  static final int kIOReturnError = 0xE00002BC;
  static final int kIOReturnNoMemory = 0xE00002BD;
  static final int kIOReturnNoResources = 0xE00002BE;
  static final int kIOReturnIPCError = 0xE00002BF;
  static final int kIOReturnNoDevice = 0xE00002C0;
  static final int kIOReturnNotPrivileged = 0xE00002C1;
  static final int kIOReturnBadArgument = 0xE00002C2;
  static final int kIOReturnLockedRead = 0xE00002C3;
  static final int kIOReturnLockedWrite = 0xE00002C4;
  static final int kIOReturnUnderrun = 0xE00002C5;
  static final int kIOReturnUnsupported = 0xE00002C7;

  /**
   * Converts an IOKit return code to its string representation.
   *
   * @param errorCode The return code to convert.
   * @return A string representation of the return code.
   */
  static String toString(int errorCode) {
    var hexResult = String.format("%08X", errorCode);
    return switch (errorCode) {
      case kIOReturnSuccess -> "kIOReturnSuccess: " + hexResult;
      case kIOReturnError -> "kIOReturnError: " + hexResult;
      case kIOReturnNoMemory -> "kIOReturnNoMemory: " + hexResult;
      case kIOReturnNoResources -> "kIOReturnNoResources: " + hexResult;
      case kIOReturnIPCError -> "kIOReturnIPCError: " + hexResult;
      case kIOReturnNoDevice -> "kIOReturnNoDevice: " + hexResult;
      case kIOReturnNotPrivileged -> "kIOReturnNotPrivileged: " + hexResult;
      case kIOReturnBadArgument -> "kIOReturnBadArgument: " + hexResult;
      case kIOReturnLockedRead -> "kIOReturnLockedRead: " + hexResult;
      case kIOReturnLockedWrite -> "kIOReturnLockedWrite: " + hexResult;
      case kIOReturnUnderrun -> "kIOReturnUnderrun" + hexResult;
      case kIOReturnUnsupported -> "kIOReturnUnsupported: " + hexResult;
      default -> hexResult;
    };
  }
}
