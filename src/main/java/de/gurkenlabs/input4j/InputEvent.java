package de.gurkenlabs.input4j;

/**
 * Represents an event that is triggered when the value of an `InputComponent` changes.
 */
public class InputEvent {
  private final InputComponent component;
  private final float oldValue;
  private final float newValue;

  /**
   * Creates a new instance of the `InputEvent` class.
   *
   * @param component the `InputComponent` whose value has changed
   * @param oldValue  the previous value of the `InputComponent`
   * @param newValue  the new value of the `InputComponent`
   */
  public InputEvent(InputComponent component, float oldValue, float newValue) {
    this.component = component;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  /**
   * Gets the `InputComponent` whose value has changed.
   *
   * @return the `InputComponent`
   */
  public InputComponent getComponent() {
    return component;
  }

  /**
   * Gets the previous value of the `InputComponent`.
   *
   * @return the old value
   */
  public float getOldValue() {
    return oldValue;
  }

  /**
   * Gets the new value of the `InputComponent`.
   *
   * @return the new value
   */
  public float getNewValue() {
    return newValue;
  }
}