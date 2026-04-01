package de.gurkenlabs.input4j.components;

import de.gurkenlabs.input4j.InputComponent;

/**
 * Predefined component identifiers for Sony DualSense controllers (PS5).
 * <p>
 * The DualSense extends the DualShock 4 with additional buttons (Create, Mic)
 * and maintains the same button/axis layout for the face buttons, triggers, and sticks.
 *
 * @see DualShock4
 */
public final class DualSense {
  private DualSense() {}

  /** Triangle button (same as DualShock 4). */
  public static final InputComponent.ID TRIANGLE = DualShock4.TRIANGLE;
  /** Circle button (same as DualShock 4). */
  public static final InputComponent.ID CIRCLE = DualShock4.CIRCLE;
  /** Cross button (same as DualShock 4). */
  public static final InputComponent.ID CROSS = DualShock4.CROSS;
  /** Square button (same as DualShock 4). */
  public static final InputComponent.ID SQUARE = DualShock4.SQUARE;

  /** L1 button (left bumper). */
  public static final InputComponent.ID L1 = DualShock4.L1;
  /** R1 button (right bumper). */
  public static final InputComponent.ID R1 = DualShock4.R1;
  /** L2 button (left trigger). */
  public static final InputComponent.ID L2 = DualShock4.L2;
  /** R2 button (right trigger). */
  public static final InputComponent.ID R2 = DualShock4.R2;

  /** Share button (same as DualShock 4 Share/Options). */
  public static final InputComponent.ID SHARE = DualShock4.SHARE;
  /** Options button (same as DualShock 4). */
  public static final InputComponent.ID OPTIONS = DualShock4.OPTIONS;

  /** Left stick button (L3). */
  public static final InputComponent.ID LEFT_THUMB_PRESS = DualShock4.LEFT_THUMB_PRESS;
  /** Right stick button (R3). */
  public static final InputComponent.ID RIGHT_THUMB_PRESS = DualShock4.RIGHT_THUMB_PRESS;

  /** PS button. */
  public static final InputComponent.ID PS = DualShock4.PS;

  /** Create button (new for DualSense). */
  public static final InputComponent.ID CREATE = new InputComponent.ID(Button.BUTTON_14, "CREATE");
  /** Microphone button (new for DualSense). */
  public static final InputComponent.ID MICROPHONE = new InputComponent.ID(Button.BUTTON_15, "MICROPHONE");

  /** Touchpad button (same as DualShock 4). */
  public static final InputComponent.ID TOUCHPAD = DualShock4.TOUCHPAD;

  /** D-pad up. */
  public static final InputComponent.ID DPAD_UP = DualShock4.DPAD_UP;
  /** D-pad down. */
  public static final InputComponent.ID DPAD_DOWN = DualShock4.DPAD_DOWN;
  /** D-pad left. */
  public static final InputComponent.ID DPAD_LEFT = DualShock4.DPAD_LEFT;
  /** D-pad right. */
  public static final InputComponent.ID DPAD_RIGHT = DualShock4.DPAD_RIGHT;

  /** Left thumbstick X-axis. */
  public static final InputComponent.ID LEFT_THUMB_X = DualShock4.LEFT_THUMB_X;
  /** Left thumbstick Y-axis. */
  public static final InputComponent.ID LEFT_THUMB_Y = DualShock4.LEFT_THUMB_Y;
  /** Right thumbstick X-axis. */
  public static final InputComponent.ID RIGHT_THUMB_X = DualShock4.RIGHT_THUMB_X;
  /** Right thumbstick Y-axis. */
  public static final InputComponent.ID RIGHT_THUMB_Y = DualShock4.RIGHT_THUMB_Y;

  /** Left trigger (analog). */
  public static final InputComponent.ID LEFT_TRIGGER = DualShock4.LEFT_TRIGGER;
  /** Right trigger (analog). */
  public static final InputComponent.ID RIGHT_TRIGGER = DualShock4.RIGHT_TRIGGER;
}