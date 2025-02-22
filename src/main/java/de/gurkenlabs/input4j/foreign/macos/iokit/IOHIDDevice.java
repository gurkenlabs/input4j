package de.gurkenlabs.input4j.foreign.macos.iokit;

class IOHIDDevice {
  long deviceAddress;
  int vendorId;
  int productId;
  String productName;
  String manufacturer;
  String transport;
  int usage;
  int usagePage;

  IOHIDDeviceInterface deviceInterface;

  @Override
  public String toString() {
    return "address: " + deviceAddress +
            ", product: '" + productName + "' (" + String.format("0X%02X", productId) + ")" +
            ", vendor: '" + manufacturer + "' (" + String.format("0X%02X", vendorId) + ")" +
            ", transport: '" + transport + '\'' +
            ", usage: " + String.format("0X%02X", usage) + " (page: " + String.format("0X%02X", usagePage) + ")";
  }

  boolean isSupportedHIDDevice() {
    return IOHIDDeviceType.GAMEPAD.isType(this.usagePage, this.usage) || IOHIDDeviceType.JOYSTICK.isType(this.usagePage, this.usage);
  }

  private enum IOHIDDeviceType {
    KEYBOARD(0x01, 0x06),
    MOUSE(0x01, 0x02),
    GAMEPAD(0x01, 0x05),
    JOYSTICK(0x01, 0x04);

    private final int primaryUsagePage;
    private final int primaryUsage;

    IOHIDDeviceType(int primaryUsagePage, int primaryUsage) {
      this.primaryUsagePage = primaryUsagePage;
      this.primaryUsage = primaryUsage;
    }

    public int getPrimaryUsagePage() {
      return primaryUsagePage;
    }

    public int getPrimaryUsage() {
      return primaryUsage;
    }

    public boolean isType(int usagePage, int usage) {
      return this.primaryUsagePage == usagePage && this.primaryUsage == usage;
    }
  }
}
