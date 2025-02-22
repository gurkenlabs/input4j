package de.gurkenlabs.input4j.foreign.macos.iokit;

public class IOHIDElement {
  long address;
  String name;
  IOHIDElementType type;
  int usage;
  int usagePage;
  int min;
  int max;

  @Override
  public String toString() {
    return "address: " + address +
            ", min: '" + min +
            ", max: '" + max +
            ", type: " + type +
            ", usage: " + String.format("0X%02X", usage) + " (page: " + String.format("0X%02X", usagePage) + ")";
  }
}
