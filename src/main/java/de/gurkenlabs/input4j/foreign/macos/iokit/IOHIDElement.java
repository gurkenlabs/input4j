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

    if (this.usage != null && this.usage != IOHIDElementUsage.UNDEFINED) {
      return this.usage.toString();
    }

    return "";
  }

  @Override
  public String toString() {
    return "address: " + address +
            ", name: '" + getName() + (this.usage != IOHIDElementUsage.UNDEFINED ? " (" + usage + ")" : "") +
            ", min: '" + min +
            ", max: '" + max +
            ", usage: " + usage +
            ", type: " + type + " (" + usagePage + ")";

  }
}
