package de.gurkenlabs.input4j.foreign.macos.iokit;

public class IOHIDElement {
  long address;
  String name;
  int type;
  int usage;
  int usagePage;
  int min;
  int max;

  @Override
  public String toString() {
    return "address: " + address +
            ", min: '" + min +
            ", max: '" + max +
            ", type: " + String.format("0X%02X", type) +
            ", usage: " + String.format("0X%02X", usage) + " (page: " + String.format("0X%02X", usagePage) + ")";
  }
}
