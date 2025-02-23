package de.gurkenlabs.input4j.foreign.macos.iokit;

/**
 * Enum representing the different usages of IOHID elements.
 *
 * <p>These values are used to identify the type of data that an IOHID element represents.
 * For example, a button element would have a usage of BUTTON_1, BUTTON_2, etc.,
 * while an axis element would have a usage of X, Y, Z, etc.</p>
 */
enum IOHIDElementUsage {
  UNDEFINED(0x00),
  BUTTON_1(0x01),
  BUTTON_2(0x02),
  BUTTON_3(0x03),
  BUTTON_4(0x04),
  BUTTON_5(0x05),
  BUTTON_6(0x06),
  BUTTON_7(0x07),
  BUTTON_8(0x08),
  BUTTON_9(0x09),
  BUTTON_10(0x0A),
  BUTTON_11(0x0B),
  BUTTON_12(0x0C),
  BUTTON_13(0x0D),
  BUTTON_14(0x0E),
  BUTTON_15(0x0F),
  BUTTON_16(0x10),
  BUTTON_17(0x11),
  BUTTON_18(0x12),
  BUTTON_19(0x13),
  BUTTON_20(0x14),
  BUTTON_21(0x15),
  BUTTON_22(0x16),
  BUTTON_23(0x17),
  BUTTON_24(0x18),
  BUTTON_25(0x19),
  BUTTON_26(0x1A),
  BUTTON_27(0x1B),
  BUTTON_28(0x1C),
  BUTTON_29(0x1D),
  BUTTON_30(0x1E),
  BUTTON_31(0x1F),
  BUTTON_32(0x20),
  X(0x30),
  Y(0x31),
  Z(0x32),
  RX(0x33),
  RY(0x34),
  RZ(0x35),
  SLIDER(0x36),
  DIAL(0x37),
  WHEEL(0x38),
  HAT_SWITCH(0x39),
  COUNTED_BUFFER(0x3A),
  BYTE_COUNT(0x3D),
  MOTION_WAKEUP(0x3F),
  START(0x40),
  SELECT(0x41),
  VX(0x42),
  VY(0x43),
  VZ(0x44),
  VBRX(0x45),
  VBRY(0x46),
  VBRZ(0x47),
  VNO(0x48),
  SYSTEM_CONTROL(0x80),
  SYSTEM_POWER_DOWN(0x81),
  SYSTEM_SLEEP(0x82),
  SYSTEM_WAKE_UP(0x83),
  SYSTEM_CONTEXT_MENU(0x84),
  SYSTEM_MAIN_MENU(0x85),
  SYSTEM_APP_MENU(0x86),
  SYSTEM_MENU_HELP(0x87),
  SYSTEM_MENU_EXIT(0x88),
  SYSTEM_MENU_SELECT(0x89),
  SYSTEM_MENU_RIGHT(0x8A),
  SYSTEM_MENU_LEFT(0x8B),
  SYSTEM_MENU_UP(0x8C),
  SYSTEM_MENU_DOWN(0x8D);

  private final int usage;

  IOHIDElementUsage(int usage) {
    this.usage = usage;
  }

  /**
   * Gets the integer value associated with the element usage.
   *
   * @return the integer value of the element usage.
   */
  int getUsage() {
    return usage;
  }

  /**
   * Returns the IOHIDElementUsage corresponding to the given integer value.
   *
   * @param value the integer value.
   * @return the corresponding IOHIDElementUsage, or UNDEFINED if no match is found.
   */
  static IOHIDElementUsage fromValue(int value) {
    for (IOHIDElementUsage usage : values()) {
      if (usage.getUsage() == value) {
        return usage;
      }
    }

    return UNDEFINED;
  }
}