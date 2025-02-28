package de.gurkenlabs.input4j.components;

import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;

public class Axis {
  public static final int MAX_DEFAULT_AXIS_ID = 7;
  public static final InputComponent.ID AXIS_X = new InputComponent.ID(ComponentType.AXIS, 0, "LEFT_AXIS_X", 0);
  public static final InputComponent.ID AXIS_Y = new InputComponent.ID(ComponentType.AXIS, 1, "LEFT_AXIS_Y", 0);
  public static final InputComponent.ID AXIS_Z = new InputComponent.ID(ComponentType.AXIS, 2, "LEFT_AXIS_Z", 0);
  public static final InputComponent.ID AXIS_RX = new InputComponent.ID(ComponentType.AXIS, 3, "RIGHT_AXIS_X", 0);
  public static final InputComponent.ID AXIS_RY = new InputComponent.ID(ComponentType.AXIS, 4, "RIGHT_AXIS_Y", 0);
  public static final InputComponent.ID AXIS_RZ = new InputComponent.ID(ComponentType.AXIS, 5, "RIGHT_AXIS_Z", 0);
  public static final InputComponent.ID AXIS_SLIDER = new InputComponent.ID(ComponentType.AXIS, 6, "SLIDER", 0);
  public static final InputComponent.ID AXIS_DPAD = new InputComponent.ID(ComponentType.AXIS, MAX_DEFAULT_AXIS_ID, "DPAD_AXIS", 0);
}
