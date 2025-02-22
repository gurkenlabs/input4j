package de.gurkenlabs.input4j.foreign.macos.iokit;

public enum IOHIDElementType {
  UNKNOWN(0),
  MISC(1),
  BUTTON(2),
  AXIS(3),
  SCANCODES(4),
  OUTPUT(5),
  FEATURE(6),
  COLLECTION(7);

  private final int value;

  IOHIDElementType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static IOHIDElementType fromValue(int value) {
    for (IOHIDElementType type : IOHIDElementType.values()) {
      if (type.value == value) {
        return type;
      }
    }
    return UNKNOWN;
  }
}