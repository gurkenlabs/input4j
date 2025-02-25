package de.gurkenlabs.input4j;

/**
 * Enum representing different types of input components.
 * <p>
 * This enum defines the various types of input components that can be used in the input system.
 * Each type represents a different kind of input, such as an axis, button, key, or an unknown type.
 * </p>
 */
public enum ComponentType {
  AXIS,
  BUTTON,
  KEY,
  UNKNOWN;

  /**
   * Checks if the component type is an axis.
   *
   * @return true if the component type is Axis, false otherwise.
   */
  public boolean isAxis() { return this == AXIS; }

  /**
   * Checks if the component type is a button.
   *
   * @return true if the component type is Button, false otherwise.
   */
  public boolean isButton() {
    return this == BUTTON;
  }
}
