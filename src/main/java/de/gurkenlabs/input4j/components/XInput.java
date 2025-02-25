package de.gurkenlabs.input4j.components;

import de.gurkenlabs.input4j.InputComponent;

public class XInput {
  public static final InputComponent.ID A = new InputComponent.ID(Button.BUTTON_0, "A");
  public static final InputComponent.ID B = new InputComponent.ID(Button.BUTTON_1, "B");
  public static final InputComponent.ID X = new InputComponent.ID(Button.BUTTON_2, "X");
  public static final InputComponent.ID Y = new InputComponent.ID(Button.BUTTON_3, "Y");
  public static final InputComponent.ID LEFT_SHOULDER = new InputComponent.ID(Button.BUTTON_4, "LEFT_SHOULDER");
  public static final InputComponent.ID RIGHT_SHOULDER = new InputComponent.ID(Button.BUTTON_5, "RIGHT_SHOULDER");
  public static final InputComponent.ID BACK = new InputComponent.ID(Button.BUTTON_6, "BACK");
  public static final InputComponent.ID START = new InputComponent.ID(Button.BUTTON_7, "START");
  public static final InputComponent.ID LEFT_THUMB = new InputComponent.ID(Button.BUTTON_8, "LEFT_THUMB");
  public static final InputComponent.ID RIGHT_THUMB = new InputComponent.ID(Button.BUTTON_9, "RIGHT_THUMB");
  public static final InputComponent.ID DPAD_UP = new InputComponent.ID(Button.DPAD_UP);
  public static final InputComponent.ID DPAD_DOWN = new InputComponent.ID(Button.DPAD_DOWN);
  public static final InputComponent.ID DPAD_LEFT = new InputComponent.ID(Button.DPAD_LEFT);
  public static final InputComponent.ID DPAD_RIGHT = new InputComponent.ID(Button.DPAD_RIGHT);
  public static final InputComponent.ID LEFT_THUMB_X = new InputComponent.ID(Axis.AXIS_X, "LEFT_THUMB_X");
  public static final InputComponent.ID LEFT_THUMB_Y = new InputComponent.ID(Axis.AXIS_Y, "LEFT_THUMB_Y");
  public static final InputComponent.ID RIGHT_THUMB_X = new InputComponent.ID(Axis.AXIS_RX, "RIGHT_THUMB_X");
  public static final InputComponent.ID RIGHT_THUMB_Y = new InputComponent.ID(Axis.AXIS_RY, "RIGHT_THUMB_Y");
  public static final InputComponent.ID LEFT_TRIGGER = new InputComponent.ID(Axis.AXIS_Z, "LEFT_TRIGGER");
  public static final InputComponent.ID RIGHT_TRIGGER = new InputComponent.ID(Axis.AXIS_RZ, "RIGHT_TRIGGER");
  public static final InputComponent.ID DPAD = new InputComponent.ID(Axis.AXIS_DPAD);
}
