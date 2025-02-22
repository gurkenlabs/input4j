package de.gurkenlabs.input4j.foreign.macos.iokit;

public enum IOHIDElementUsagePage {
  UNDEFINED(0x00),
  GENERIC_DESKTOP(0x01),
  SIMULATION(0x02),
  VR(0x03),
  SPORT(0x04),
  GAME(0x05),
  GENERIC_DEVICE(0x06),
  KEYBOARD(0x07),
  LED(0x08),
  BUTTON(0x09),
  ORDINAL(0x0A);

  private final int value;
  IOHIDElementUsagePage(int value) {
    this.value = value;
  }

  public static IOHIDElementUsagePage fromValue(int value) {
    for (IOHIDElementUsagePage page : IOHIDElementUsagePage.values()) {
      if (page.value == value) {
        return page;
      }
    }
    return UNDEFINED;
  }
}
