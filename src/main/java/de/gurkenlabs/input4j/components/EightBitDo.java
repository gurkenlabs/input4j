package de.gurkenlabs.input4j.components;

import de.gurkenlabs.input4j.InputComponent;

/**
 * Predefined component identifiers for 8BitDo controllers.
 * <p>
 * 8BitDo controllers come in various layouts depending on the model:
 * <ul>
 *   <li>SF30 Pro, SN30 Pro+: Classic gamepad layout (like SNES)</li>
 *   <li>N30 Pro, F30 Pro: Modern gamepad layout</li>
 *   <li>Zero: Ultra-compact layout</li>
 *   <li>N30 Arcade: Arcade stick layout</li>
 * </ul>
 * <p>
 * This class provides unified button mappings that work across most models,
 * with specific variants for different controller families.
 */
public final class EightBitDo {
  private EightBitDo() {}

  /** Model family: SF30 Pro / SN30 Pro+ (classic layout). */
  public static final class Pro {
    private Pro() {}

    /** A button (right). */
    public static final InputComponent.ID A = XInput.A;
    /** B button (bottom). */
    public static final InputComponent.ID B = XInput.B;
    /** X button (left). */
    public static final InputComponent.ID X = XInput.X;
    /** Y button (top). */
    public static final InputComponent.ID Y = XInput.Y;

    /** L button (left bumper). */
    public static final InputComponent.ID L = XInput.LEFT_SHOULDER;
    /** R button (right bumper). */
    public static final InputComponent.ID R = XInput.RIGHT_SHOULDER;
    /** ZL button (left trigger). */
    public static final InputComponent.ID ZL = new InputComponent.ID(Button.BUTTON_6, "ZL");
    /** ZR button (right trigger). */
    public static final InputComponent.ID ZR = new InputComponent.ID(Button.BUTTON_7, "ZR");

    /** Select button. */
    public static final InputComponent.ID SELECT = XInput.BACK;
    /** Start button. */
    public static final InputComponent.ID START = XInput.START;

    /** Left stick click. */
    public static final InputComponent.ID LEFT_STICK = XInput.LEFT_THUMB;
    /** Right stick click. */
    public static final InputComponent.ID RIGHT_STICK = XInput.RIGHT_THUMB;

    /** D-pad up. */
    public static final InputComponent.ID DPAD_UP = XInput.DPAD_UP;
    /** D-pad down. */
    public static final InputComponent.ID DPAD_DOWN = XInput.DPAD_DOWN;
    /** D-pad left. */
    public static final InputComponent.ID DPAD_LEFT = XInput.DPAD_LEFT;
    /** D-pad right. */
    public static final InputComponent.ID DPAD_RIGHT = XInput.DPAD_RIGHT;

    /** Left stick X-axis. */
    public static final InputComponent.ID LEFT_STICK_X = XInput.LEFT_THUMB_X;
    /** Left stick Y-axis. */
    public static final InputComponent.ID LEFT_STICK_Y = XInput.LEFT_THUMB_Y;
    /** Right stick X-axis. */
    public static final InputComponent.ID RIGHT_STICK_X = XInput.RIGHT_THUMB_X;
    /** Right stick Y-axis. */
    public static final InputComponent.ID RIGHT_STICK_Y = XInput.RIGHT_THUMB_Y;

    /** Left trigger (analog). */
    public static final InputComponent.ID LEFT_TRIGGER = XInput.LEFT_TRIGGER;
    /** Right trigger (analog). */
    public static final InputComponent.ID RIGHT_TRIGGER = XInput.RIGHT_TRIGGER;
  }

  /** Model family: N30 Pro, F30 Pro (modern layout). */
  public static final class Modern {
    private Modern() {}

    /** A button. */
    public static final InputComponent.ID A = XInput.A;
    /** B button. */
    public static final InputComponent.ID B = XInput.B;
    /** X button. */
    public static final InputComponent.ID X = XInput.X;
    /** Y button. */
    public static final InputComponent.ID Y = XInput.Y;

    /** L button. */
    public static final InputComponent.ID L = Button.BUTTON_4;
    /** R button. */
    public static final InputComponent.ID R = Button.BUTTON_5;
    /** L2 button. */
    public static final InputComponent.ID L2 = Button.BUTTON_6;
    /** R2 button. */
    public static final InputComponent.ID R2 = Button.BUTTON_7;

    /** Share button. */
    public static final InputComponent.ID SHARE = Button.BUTTON_8;
    /** Options button. */
    public static final InputComponent.ID OPTIONS = Button.BUTTON_9;

    /** Left stick click. */
    public static final InputComponent.ID LEFT_STICK = Button.BUTTON_10;
    /** Right stick click. */
    public static final InputComponent.ID RIGHT_STICK = Button.BUTTON_11;

    /** Home button. */
    public static final InputComponent.ID HOME = Button.BUTTON_12;

    /** D-pad up. */
    public static final InputComponent.ID DPAD_UP = Button.DPAD_UP;
    /** D-pad down. */
    public static final InputComponent.ID DPAD_DOWN = Button.DPAD_DOWN;
    /** D-pad left. */
    public static final InputComponent.ID DPAD_LEFT = Button.DPAD_LEFT;
    /** D-pad right. */
    public static final InputComponent.ID DPAD_RIGHT = Button.DPAD_RIGHT;

    /** Left stick X-axis. */
    public static final InputComponent.ID LEFT_STICK_X = Axis.AXIS_X;
    /** Left stick Y-axis. */
    public static final InputComponent.ID LEFT_STICK_Y = Axis.AXIS_Y;
    /** Right stick X-axis. */
    public static final InputComponent.ID RIGHT_STICK_X = Axis.AXIS_RX;
    /** Right stick Y-axis. */
    public static final InputComponent.ID RIGHT_STICK_Y = Axis.AXIS_RY;
  }

  /** Model family: Zero (ultra-compact). */
  public static final class Zero {
    private Zero() {}

    /** A button. */
    public static final InputComponent.ID A = new InputComponent.ID(Button.BUTTON_0, "A");
    /** B button. */
    public static final InputComponent.ID B = new InputComponent.ID(Button.BUTTON_1, "B");
    /** X button. */
    public static final InputComponent.ID X = new InputComponent.ID(Button.BUTTON_2, "X");
    /** Y button. */
    public static final InputComponent.ID Y = new InputComponent.ID(Button.BUTTON_3, "Y");

    /** L button. */
    public static final InputComponent.ID L = new InputComponent.ID(Button.BUTTON_4, "L");
    /** R button. */
    public static final InputComponent.ID R = new InputComponent.ID(Button.BUTTON_5, "R");

    /** Select button. */
    public static final InputComponent.ID SELECT = new InputComponent.ID(Button.BUTTON_8, "SELECT");
    /** Start button. */
    public static final InputComponent.ID START = new InputComponent.ID(Button.BUTTON_9, "START");

    /** D-pad up. */
    public static final InputComponent.ID DPAD_UP = Button.DPAD_UP;
    /** D-pad down. */
    public static final InputComponent.ID DPAD_DOWN = Button.DPAD_DOWN;
    /** D-pad left. */
    public static final InputComponent.ID DPAD_LEFT = Button.DPAD_LEFT;
    /** D-pad right. */
    public static final InputComponent.ID DPAD_RIGHT = Button.DPAD_RIGHT;

    /** Left stick X-axis. */
    public static final InputComponent.ID LEFT_STICK_X = Axis.AXIS_X;
    /** Left stick Y-axis. */
    public static final InputComponent.ID LEFT_STICK_Y = Axis.AXIS_Y;
  }
}