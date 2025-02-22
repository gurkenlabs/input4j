package de.gurkenlabs.input4j.foreign.macos.iokit;

class IOHIDElement {
  long address;
  String name;
  IOHIDElementType type;
  IOHIDElementUsage usage;
  IOHIDElementUsagePage usagePage;
  int min;
  int max;
  int physicalMin;
  int physicalMax;
  int unit;
  int unitExponent;

  String getName() {
    if (this.name != null) {
      return this.name;
    }

    return usage.toString();
  }

  @Override
  public String toString() {
    return "address: " + address +
            ", name: '" + getName() +
            ", min: '" + min +
            ", max: '" + max +
            ", type: " + type +
            ", usage: " + usage + " (" + usagePage + ")";
  }
}
