package de.gurkenlabs.input4j.components;

import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;

import static de.gurkenlabs.input4j.components.Button.MAX_BUTTON_ID;

public class Axis {
  public static final int MAX_AXIS_ID = MAX_BUTTON_ID + 8;
  public static final InputComponent.ID AXIS_X = new InputComponent.ID(ComponentType.AXIS, MAX_BUTTON_ID + 1, "LEFT_AXIS_X", 0);
  public static final InputComponent.ID AXIS_Y = new InputComponent.ID(ComponentType.AXIS, MAX_BUTTON_ID + 2, "LEFT_AXIS_Y", 0);
  public static final InputComponent.ID AXIS_Z = new InputComponent.ID(ComponentType.AXIS, MAX_BUTTON_ID + 3, "LEFT_AXIS_Z", 0);
  public static final InputComponent.ID AXIS_RX = new InputComponent.ID(ComponentType.AXIS, MAX_BUTTON_ID + 4, "RIGHT_AXIS_X", 0);
  public static final InputComponent.ID AXIS_RY = new InputComponent.ID(ComponentType.AXIS, MAX_BUTTON_ID + 5, "RIGHT_AXIS_Y", 0);
  public static final InputComponent.ID AXIS_RZ = new InputComponent.ID(ComponentType.AXIS, MAX_BUTTON_ID + 6, "RIGHT_AXIS_Z", 0);
  public static final InputComponent.ID AXIS_SLIDER = new InputComponent.ID(ComponentType.AXIS, MAX_BUTTON_ID + 7, "SLIDER", 0);
  public static final InputComponent.ID AXIS_DPAD = new InputComponent.ID(ComponentType.AXIS, MAX_AXIS_ID, "DPAD_AXIS", 0);
}
