package de.gurkenlabs.input4j.foreign.macos.iokit;

/**
 * Enum representing the different usage pages of IOHID elements.
 *
 * <p>Usage pages are used to group related usages together. Each usage page defines a set of usages
 * that are relevant to a particular category of devices or controls. For example, the GENERIC_DESKTOP
 * usage page includes usages for common input controls like joysticks, gamepads, and keyboards.</p>
 *
 * <p>Each usage page is identified by a unique integer value. The usages within a usage page are
 * also identified by unique integer values, and these values are used to identify the type of data
 * that an IOHID element represents.</p>
 *
 * <p>The {@link IOHIDElementUsage} enum defines the specific usages within these usage pages. For example,
 * the GENERIC_DESKTOP usage page includes usages like X, Y, Z, RX, RY, and RZ, which represent the
 * different axes of a joystick or gamepad.</p>
 */
enum IOHIDElementUsagePage {
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

  static IOHIDElementUsagePage fromValue(int value) {
    for (IOHIDElementUsagePage page : IOHIDElementUsagePage.values()) {
      if (page.value == value) {
        return page;
      }
    }
    return UNDEFINED;
  }
}
