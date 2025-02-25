package de.gurkenlabs.input4j.components;

import de.gurkenlabs.input4j.InputComponent;

public class DualShock4 {
  public static final InputComponent.ID SQUARE = new InputComponent.ID(Button.BUTTON_0, "SQUARE");
  public static final InputComponent.ID CROSS = new InputComponent.ID(Button.BUTTON_1, "CROSS");
  public static final InputComponent.ID CIRCLE = new InputComponent.ID(Button.BUTTON_2, "CIRCLE");
  public static final InputComponent.ID TRIANGLE = new InputComponent.ID(Button.BUTTON_3, "TRIANGLE");
  public static final InputComponent.ID L1 = new InputComponent.ID(Button.BUTTON_4, "L1");
  public static final InputComponent.ID R1 = new InputComponent.ID(Button.BUTTON_5, "R1");
  public static final InputComponent.ID L2 = new InputComponent.ID(Button.BUTTON_6, "L2");
  public static final InputComponent.ID R2 = new InputComponent.ID(Button.BUTTON_7, "R2");
  public static final InputComponent.ID SHARE = new InputComponent.ID(Button.BUTTON_8, "SHARE");
  public static final InputComponent.ID OPTIONS = new InputComponent.ID(Button.BUTTON_9, "OPTIONS");
  public static final InputComponent.ID LEFT_THUMB_PRESS = new InputComponent.ID(Button.BUTTON_10, "LEFT_THUMB_PRESS");
  public static final InputComponent.ID RIGHT_THUMB_PRESS = new InputComponent.ID(Button.BUTTON_11, "RIGHT_THUMB_PRESS");
  public static final InputComponent.ID PS = new InputComponent.ID(Button.BUTTON_12, "PS");
  public static final InputComponent.ID TOUCHPAD = new InputComponent.ID(Button.BUTTON_13, "TOUCHPAD");
  public static final InputComponent.ID DPAD_UP = new InputComponent.ID(Button.DPAD_UP);
  public static final InputComponent.ID DPAD_DOWN = new InputComponent.ID(Button.DPAD_DOWN);
  public static final InputComponent.ID DPAD_LEFT = new InputComponent.ID(Button.DPAD_LEFT);
  public static final InputComponent.ID DPAD_RIGHT = new InputComponent.ID(Button.DPAD_RIGHT);
  public static final InputComponent.ID DPAD = new InputComponent.ID(Axis.AXIS_DPAD);
  public static final InputComponent.ID LEFT_THUMB_X = new InputComponent.ID(Axis.AXIS_X, "LEFT_THUMB_X");
  public static final InputComponent.ID LEFT_THUMB_Y = new InputComponent.ID(Axis.AXIS_Y, "LEFT_THUMB_Y");
  public static final InputComponent.ID RIGHT_THUMB_X = new InputComponent.ID(Axis.AXIS_Z, "RIGHT_THUMB_X"); // this is actually the Z axis on the DS4
  public static final InputComponent.ID RIGHT_THUMB_Y = new InputComponent.ID(Axis.AXIS_RZ, "RIGHT_THUMB_Y"); // this is actually the RZ axis on the DS4
  public static final InputComponent.ID LEFT_TRIGGER = new InputComponent.ID(Axis.AXIS_RX, "LEFT_TRIGGER");
  public static final InputComponent.ID RIGHT_TRIGGER = new InputComponent.ID(Axis.AXIS_RY, "RIGHT_TRIGGER");
}
