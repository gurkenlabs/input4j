package de.gurkenlabs.input4j.components;

import de.gurkenlabs.input4j.InputComponent;

/**
 * Predefined component identifiers for Sony DualShock 4 controllers.
 */
public class DualShock4 {
  /** Square button. */
  public static final InputComponent.ID SQUARE = new InputComponent.ID(Button.BUTTON_0, "SQUARE");
  /** Cross button. */
  public static final InputComponent.ID CROSS = new InputComponent.ID(Button.BUTTON_1, "CROSS");
  /** Circle button. */
  public static final InputComponent.ID CIRCLE = new InputComponent.ID(Button.BUTTON_2, "CIRCLE");
  /** Triangle button. */
  public static final InputComponent.ID TRIANGLE = new InputComponent.ID(Button.BUTTON_3, "TRIANGLE");
  /** L1 button (left bumper). */
  public static final InputComponent.ID L1 = new InputComponent.ID(Button.BUTTON_4, "L1");
  /** R1 button (right bumper). */
  public static final InputComponent.ID R1 = new InputComponent.ID(Button.BUTTON_5, "R1");
  /** L2 button (left trigger). */
  public static final InputComponent.ID L2 = new InputComponent.ID(Button.BUTTON_6, "L2");
  /** R2 button (right trigger). */
  public static final InputComponent.ID R2 = new InputComponent.ID(Button.BUTTON_7, "R2");
  /** Share button. */
  public static final InputComponent.ID SHARE = new InputComponent.ID(Button.BUTTON_8, "SHARE");
  /** Options button. */
  public static final InputComponent.ID OPTIONS = new InputComponent.ID(Button.BUTTON_9, "OPTIONS");
  /** Left stick button (L3). */
  public static final InputComponent.ID LEFT_THUMB_PRESS = new InputComponent.ID(Button.BUTTON_10, "LEFT_THUMB_PRESS");
  /** Right stick button (R3). */
  public static final InputComponent.ID RIGHT_THUMB_PRESS = new InputComponent.ID(Button.BUTTON_11, "RIGHT_THUMB_PRESS");
  /** PlayStation button. */
  public static final InputComponent.ID PS = new InputComponent.ID(Button.BUTTON_12, "PS");
  /** Touchpad button. */
  public static final InputComponent.ID TOUCHPAD = new InputComponent.ID(Button.BUTTON_13, "TOUCHPAD");
  /** D-pad up. */
  public static final InputComponent.ID DPAD_UP = new InputComponent.ID(Button.DPAD_UP);
  /** D-pad down. */
  public static final InputComponent.ID DPAD_DOWN = new InputComponent.ID(Button.DPAD_DOWN);
  /** D-pad left. */
  public static final InputComponent.ID DPAD_LEFT = new InputComponent.ID(Button.DPAD_LEFT);
  /** D-pad right. */
  public static final InputComponent.ID DPAD_RIGHT = new InputComponent.ID(Button.DPAD_RIGHT);
  /** D-pad as axis (for analog d-pads). */
  public static final InputComponent.ID DPAD = new InputComponent.ID(Axis.AXIS_DPAD);
  /** Left thumbstick X-axis. */
  public static final InputComponent.ID LEFT_THUMB_X = new InputComponent.ID(Axis.AXIS_X, "LEFT_THUMB_X");
  /** Left thumbstick Y-axis. */
  public static final InputComponent.ID LEFT_THUMB_Y = new InputComponent.ID(Axis.AXIS_Y, "LEFT_THUMB_Y");
  /** Right thumbstick X-axis (Z axis on DS4). */
  public static final InputComponent.ID RIGHT_THUMB_X = new InputComponent.ID(Axis.AXIS_Z, "RIGHT_THUMB_X"); // this is actually the Z axis on the DS4
  /** Right thumbstick Y-axis (RZ axis on DS4). */
  public static final InputComponent.ID RIGHT_THUMB_Y = new InputComponent.ID(Axis.AXIS_RZ, "RIGHT_THUMB_Y"); // this is actually the RZ axis on the DS4
  /** Left trigger (RX axis on DS4). */
  public static final InputComponent.ID LEFT_TRIGGER = new InputComponent.ID(Axis.AXIS_RX, "LEFT_TRIGGER");
  /** Right trigger (RY axis on DS4). */
  public static final InputComponent.ID RIGHT_TRIGGER = new InputComponent.ID(Axis.AXIS_RY, "RIGHT_TRIGGER");
}
