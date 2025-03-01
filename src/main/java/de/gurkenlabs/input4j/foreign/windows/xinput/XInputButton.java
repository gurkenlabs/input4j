package de.gurkenlabs.input4j.foreign.windows.xinput;

/**
 * The {@code XInputButton} enum represents the buttons on an XInput gamepad.
 * Each enum constant corresponds to a specific button and its associated value.
 */
public enum XInputButton {
  DPAD_UP(0x0001),
  DPAD_DOWN(0x0002),
  DPAD_LEFT(0x0004),
  DPAD_RIGHT(0x0008),
  START(0x0010),
  BACK(0x0020),
  LEFT_THUMB(0x0040),
  RIGHT_THUMB(0x0080),
  LEFT_SHOULDER(0x0100),
  RIGHT_SHOULDER(0x0200),
  A(0x1000),
  B(0x2000),
  X(0x4000),
  Y(0x8000);

  /**
   * Cache the values array to avoid creating a new array each time values() is called.
   */
  static final XInputButton[] values = values();
  private final int value;

  XInputButton(int value) {
    this.value = value;
  }

  /**
   * Gets the value associated with the button.
   *
   * @return The value of the button.
   */
  public int getValue() {
    return value;
  }

  /**
   * Checks if the button is pressed based on the given button state.
   *
   * @param wButtons The button state to check against.
   * @return {@code true} if the button is pressed, {@code false} otherwise.
   */
  public boolean isPressed(int wButtons) {
    return (wButtons & this.value) != 0;
  }
}
