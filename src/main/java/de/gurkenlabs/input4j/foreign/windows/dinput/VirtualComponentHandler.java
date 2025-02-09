package de.gurkenlabs.input4j.foreign.windows.dinput;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The `VirtualComponentHandler` class provides utility methods to handle virtual components
 * for input devices, particularly for DirectInput devices. It prepares virtual components
 * and handles polled values for these components.
 */
final class VirtualComponentHandler {
  private VirtualComponentHandler() {
  }

  /**
   * Prepares virtual components for the specified input device by adding additional components
   * for D-Pad and Z-Axis if necessary.
   *
   * @param device     the input device
   * @param components the collection of input components
   */
  static void prepareVirtualComponents(final InputDevice device, final Collection<InputComponent> components) {
    var allComponents = new ArrayList<>(components);
    if (components.stream().anyMatch(x -> x.getId().equals(InputComponent.Axis.DPAD))) {
      // In DirectInput, the D-Pad is reported as a single Hat Switch (angles or -1 for centered).
      // This requires splitting the Hat Switch into components (DPAD_UP, DPAD_DOWN, etc.) in the unified model of the input4J library.
      // We need to add 4 more button components to account for this
      allComponents.add(new InputComponent(device, InputComponent.Dpad.UP));
      allComponents.add(new InputComponent(device, InputComponent.Dpad.DOWN));
      allComponents.add(new InputComponent(device, InputComponent.Dpad.LEFT));
      allComponents.add(new InputComponent(device, InputComponent.Dpad.RIGHT));
    }

    if (components.stream().anyMatch(x -> x.getId().equals(InputComponent.Axis.Z))) {
      // In DirectInput, both triggers are mapped to the same (left) Z-Axis, with LEFT_TRIGGER using the positive range (0 to 1)
      // and RIGHT_TRIGGER using the negative range (-1 to 0). In Linux and XInput, the triggers are separate and have their own axis and same value range.
      // We need to separate this component into two virtual components that reflect the triggers individually
      allComponents.add(new InputComponent(device, InputComponent.Axis.RZ));
    }

    device.setComponents(allComponents);
  }

  /**
   * Handles the polled values for the specified input device by mapping native values
   * to the appropriate virtual components.
   *
   * @param device       the input device
   * @param nativeValues the array of native values
   * @return an array of values mapped to the virtual components
   */
  static float[] handlePolledValues(final InputDevice device, final float[] nativeValues) {
    var allValues = new float[device.getComponents().size()];
    var dpadUpIndex = findComponentIndex(device, allValues, InputComponent.Dpad.UP);
    var dpadDownIndex = findComponentIndex(device, allValues, InputComponent.Dpad.DOWN);
    var dpadLeftIndex = findComponentIndex(device, allValues, InputComponent.Dpad.LEFT);
    var dpadRightIndex = findComponentIndex(device, allValues, InputComponent.Dpad.RIGHT);
    var rzAxisIndex = findComponentIndex(device, allValues, InputComponent.Axis.RZ);

    for (int i = 0; i < nativeValues.length && i < device.getComponents().size(); i++) {
      var value = nativeValues[i];
      if (value == 0) {
        continue;
      }

      allValues[i] = value;

      var component = device.getComponents().get(i);
      if (component == null) {
        continue;
      }

      if (component.getId().equals(InputComponent.Axis.Z) && value < 0) {
        allValues[i] = 0;
        if (rzAxisIndex != -1) allValues[rzAxisIndex] = Math.abs(value);
        continue;
      }

      if (component.getId().equals(InputComponent.Axis.DPAD)) {
        allValues[i] = 0;

        if (value == DPAD_VALUE.UP) {
          if (dpadUpIndex != -1) allValues[dpadUpIndex] = 1;
        } else if (value == DPAD_VALUE.DOWN) {
          if (dpadDownIndex != -1) allValues[dpadDownIndex] = 1;
        } else if (value == DPAD_VALUE.LEFT) {
          if (dpadLeftIndex != -1) allValues[dpadLeftIndex] = 1;
        } else if (value == DPAD_VALUE.RIGHT) {
          if (dpadRightIndex != -1) allValues[dpadRightIndex] = 1;
        } else if (value == DPAD_VALUE.UP_LEFT) {
          if (dpadUpIndex != -1) allValues[dpadUpIndex] = 1;
          if (dpadLeftIndex != -1) allValues[dpadLeftIndex] = 1;
        } else if (value == DPAD_VALUE.UP_RIGHT) {
          if (dpadUpIndex != -1) allValues[dpadUpIndex] = 1;
          if (dpadRightIndex != -1) allValues[dpadRightIndex] = 1;
        } else if (value == DPAD_VALUE.DOWN_LEFT) {
          if (dpadDownIndex != -1) allValues[dpadDownIndex] = 1;
          if (dpadLeftIndex != -1) allValues[dpadLeftIndex] = 1;
        } else if (value == DPAD_VALUE.DOWN_RIGHT) {
          if (dpadDownIndex != -1) allValues[dpadDownIndex] = 1;
          if (dpadRightIndex != -1) allValues[dpadRightIndex] = 1;
        }
      }
    }

    return allValues;
  }

  /**
   * Finds the index of the specified component in the input device.
   *
   * @param device the input device
   * @param values the array of values
   * @param id     the component ID
   * @return the index of the component, or -1 if not found
   */
  private static int findComponentIndex(InputDevice device, float[] values, InputComponent.ID id) {
    for (int i = 0; i < device.getComponents().size() && i < values.length; i++) {
      if (device.getComponents().get(i).getId().equals(id)) {
        return i;
      }
    }

    return -1;
  }

  /**
   * The `DPAD_VALUE` class defines standard values for D-Pad DirectInput values.
   */
  private static class DPAD_VALUE {
    public static final float UP_LEFT = 0.125f;
    public static final float UP = 0.25f;
    public static final float UP_RIGHT = 0.375f;
    public static final float RIGHT = 0.50f;
    public static final float DOWN_RIGHT = 0.625f;
    public static final float DOWN = 0.75f;
    public static final float DOWN_LEFT = 0.875f;
    public static final float LEFT = 1.0f;
  }
}
