package de.gurkenlabs.input4j.foreign.macos.iokit;

/**
 * Enum representing the different types of IOHID elements.
 */
enum IOHIDElementType {
  UNDEFINED(0),
  MISC(1),
  BUTTON(2),
  AXIS(3),
  SCANCODES(4),
  OUTPUT(5),
  FEATURE(6),
  COLLECTION(7);

  /**
   * Cache the values array to avoid creating a new array each time values() is called.
   */
  private static final IOHIDElementType[] values = values();

  private final int value;

  IOHIDElementType(int value) {
    this.value = value;
  }

  /**
   * Gets the integer value associated with the element type.
   *
   * @return the integer value of the element type.
   */
  int getValue() {
    return value;
  }

  /**
   * Returns the IOHIDElementType corresponding to the given integer value.
   *
   * @param value the integer value.
   * @return the corresponding IOHIDElementType, or UNDEFINED if no match is found.
   */
  static IOHIDElementType fromValue(int value) {
    for (IOHIDElementType type : IOHIDElementType.values) {
      if (type.value == value) {
        return type;
      }
    }
    return UNDEFINED;
  }
}
