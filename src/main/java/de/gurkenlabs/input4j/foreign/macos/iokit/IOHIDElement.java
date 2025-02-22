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

    if (this.getUsage() != null && this.getUsage() != IOHIDElementUsage.UNDEFINED) {
      return this.getUsage().toString();
    }

    return "";
  }

  IOHIDElementUsage getUsage() {
    if (this.type != IOHIDElementType.BUTTON && this.usage.getUsage() < IOHIDElementUsage.BUTTON_32.getUsage()) {
      return IOHIDElementUsage.UNDEFINED;
    }
    return this.usage;
  }

  @Override
  public String toString() {
    return "address: " + address +
            ", name: '" + getName() + "'" +
            ", usage: " + getUsage() +
            ", type: " + type + " (" + usagePage + ")" +
            ", min: '" + min +
            ", max: '" + max;
  }
}
