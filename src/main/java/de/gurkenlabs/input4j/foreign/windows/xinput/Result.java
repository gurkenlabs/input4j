package de.gurkenlabs.input4j.foreign.windows.xinput;

/**
 * The `Result` class defines constants for various XInput error codes.
 * These constants represent different result codes that can be returned by XInput functions.
 */
class Result {
  /**
   * The operation completed successfully.
   */
  static final int ERROR_SUCCESS = 0x00000000;

  /**
   * The device is not connected.
   */
  static final int ERROR_DEVICE_NOT_CONNECTED = 0x0000048F;

  /**
   * An invalid parameter was passed to the returning function.
   */
  static final int ERROR_BAD_ARGUMENTS = 0x000000A0;

  /**
   * Converts an XInput error code to its string representation.
   *
   * @param errorCode The error code to convert.
   * @return A string representation of the error code.
   */
  static String toString(int errorCode) {
    var hexResult = String.format("%08X", errorCode);
    return switch (errorCode) {
      case ERROR_SUCCESS -> "ERROR_SUCCESS: " + hexResult;
      case ERROR_DEVICE_NOT_CONNECTED -> "ERROR_DEVICE_NOT_CONNECTED: " + hexResult;
      case ERROR_BAD_ARGUMENTS -> "ERROR_BAD_ARGUMENTS: " + hexResult;
      default -> hexResult;
    };
  }
}
