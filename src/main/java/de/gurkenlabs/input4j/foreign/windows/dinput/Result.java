package de.gurkenlabs.input4j.foreign.windows.dinput;

/**
 * The `Result` class defines constants for various DirectInput HRESULT values and provides a method to
 * convert these values to their string representations. These constants represent different result codes
 * that can be returned by DirectInput functions.
 */
class Result {
  /**
   * Operation completed successfully.
   */
  static final int DI_OK = 0x00000000;

  /**
   * An invalid parameter was passed to the returning function, or the object was not in a state that permitted the function to be called.
   */
  static final int DIERR_INVALIDPARAM = 0x80070057;

  /**
   * This object has not been initialized.
   */
  static final int DIERR_NOTINITIALIZED = 0x80070015;

  /**
   * The device buffer overflowed and some input was lost.
   */
  static final int DI_BUFFEROVERFLOW = 0x00000001;

  /**
   * Access to the input device has been lost. It must be reacquired.
   */
  static final int DIERR_INPUTLOST = 0x8007001E;

  /**
   * The operation cannot be performed unless the device is acquired.
   */
  static final int DIERR_NOTACQUIRED = 0x8007000C;

  /**
   * Another application has a higher priority level, preventing this call from succeeding.
   */
  static final int DIERR_OTHERAPPHASPRIO = 0x80070005;

  static final int E_NOTIMPLEMENTED = 0x80004001;

  /**
   * Converts a DirectInput HRESULT value to its string representation.
   *
   * @param HRESULT The HRESULT value to convert.
   * @return A string representation of the HRESULT value.
   */
  static String toString(int HRESULT) {
    var hexResult = String.format("%08X", HRESULT);
    return switch (HRESULT) {
      case DI_OK -> "DI_OK: " + hexResult;
      case DIERR_INVALIDPARAM -> "DIERR_INVALIDPARAM: " + hexResult;
      case DIERR_NOTINITIALIZED -> "DIERR_NOTINITIALIZED: " + hexResult;
      case DI_BUFFEROVERFLOW -> "DI_BUFFEROVERFLOW: " + hexResult;
      case DIERR_INPUTLOST -> "DIERR_INPUTLOST: " + hexResult;
      case DIERR_NOTACQUIRED -> "DIERR_NOTACQUIRED: " + hexResult;
      case DIERR_OTHERAPPHASPRIO -> "DIERR_OTHERAPPHASPRIO: " + hexResult;
      case E_NOTIMPLEMENTED -> "E_NOTIMPLEMENTED: " + hexResult;

      default -> hexResult;
    };

  }
}
