package de.gurkenlabs.input4j;

import java.util.EventListener;

/**
 * The `InputDeviceListener` interface should be implemented by any class that wants to receive notifications
 * when the value of an `InputComponent` changes.
 * <p>
 * This is a functional interface and can therefore be used as the assignment target for a lambda expression
 * or method reference.
 */
@FunctionalInterface
public interface InputDeviceListener extends EventListener {
  /**
   * Invoked when the value of an `InputComponent` changes.
   *
   * @param component the `InputComponent` whose value has changed
   * @param oldValue  the previous value of the `InputComponent`
   * @param newValue  the new value of the `InputComponent`
   */
  void onValueChanged(InputComponent component, float oldValue, float newValue);
}
