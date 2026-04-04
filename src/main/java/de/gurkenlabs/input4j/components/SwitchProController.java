package de.gurkenlabs.input4j.components;

import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;

/**
 * Predefined component identifiers for Nintendo Switch Pro Controller.
 * <p>
 * The Switch Pro Controller has a different button layout than Xbox/PlayStation:
 * - Face buttons: B (A), A (S), Y (W), X (N) in Switch order
 * - Additional buttons: +, -, Home, Capture
 * - Full D-pad + shoulder buttons
 */
public final class SwitchProController {
  private SwitchProController() {}

  /** B button (right of A). */
  public static final InputComponent.ID B = new InputComponent.ID(Button.BUTTON_0, "B");
  /** A button (below B). */
  public static final InputComponent.ID A = new InputComponent.ID(Button.BUTTON_1, "A");
  /** Y button (left of X). */
  public static final InputComponent.ID Y = new InputComponent.ID(Button.BUTTON_2, "Y");
  /** X button (above A). */
  public static final InputComponent.ID X = new InputComponent.ID(Button.BUTTON_3, "X");

  /** L button (left bumper). */
  public static final InputComponent.ID L = Button.BUTTON_4;
  /** R button (right bumper). */
  public static final InputComponent.ID R = Button.BUTTON_5;
  /** ZL button (left trigger, analog). */
  public static final InputComponent.ID ZL = Button.BUTTON_6;
  /** ZR button (right trigger, analog). */
  public static final InputComponent.ID ZR = Button.BUTTON_7;

  /** Plus button. */
  public static final InputComponent.ID PLUS = Button.BUTTON_8;
  /** Minus button. */
  public static final InputComponent.ID MINUS = Button.BUTTON_9;

  /** Left stick click (L3). */
  public static final InputComponent.ID LEFT_STICK = Button.BUTTON_10;
  /** Right stick click (R3). */
  public static final InputComponent.ID RIGHT_STICK = Button.BUTTON_11;

  /** Home button. */
  public static final InputComponent.ID HOME = Button.BUTTON_12;
  /** Capture button. */
  public static final InputComponent.ID CAPTURE = Button.BUTTON_13;

  /** D-pad up. */
  public static final InputComponent.ID DPAD_UP = Button.DPAD_UP;
  /** D-pad right. */
  public static final InputComponent.ID DPAD_RIGHT = Button.DPAD_RIGHT;
  /** D-pad down. */
  public static final InputComponent.ID DPAD_DOWN = Button.DPAD_DOWN;
  /** D-pad left. */
  public static final InputComponent.ID DPAD_LEFT = Button.DPAD_LEFT;

  /** Left stick X-axis. */
  public static final InputComponent.ID LEFT_STICK_X = Axis.AXIS_X;
  /** Left stick Y-axis. */
  public static final InputComponent.ID LEFT_STICK_Y = Axis.AXIS_Y;
  /** Right stick X-axis. */
  public static final InputComponent.ID RIGHT_STICK_X = Axis.AXIS_RX;
  /** Right stick Y-axis. */
  public static final InputComponent.ID RIGHT_STICK_Y = Axis.AXIS_RY;

  /** Left trigger (ZL, analog). */
  public static final InputComponent.ID LEFT_TRIGGER = Axis.AXIS_Z;
  /** Right trigger (ZR, analog). */
  public static final InputComponent.ID RIGHT_TRIGGER = Axis.AXIS_RZ;
}