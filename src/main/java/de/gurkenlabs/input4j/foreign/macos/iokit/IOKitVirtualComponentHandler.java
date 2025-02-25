package de.gurkenlabs.input4j.foreign.macos.iokit;


import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.components.Axis;
import de.gurkenlabs.input4j.components.Button;

import java.util.ArrayList;
import java.util.Collection;

public class IOKitVirtualComponentHandler {

  private IOKitVirtualComponentHandler() {
  }

  /**
   * Prepares virtual components for the specified input device by adding additional components
   * for D-Pad if necessary.
   *
   * @param device     the input device
   * @param components the collection of input components
   */
  static void prepareVirtualComponents(final InputDevice device, final Collection<InputComponent> components) {
    var allComponents = new ArrayList<>(components);

    // Add D-Pad components if necessary
    if (components.stream().anyMatch(x -> x.getId().equals(Axis.AXIS_DPAD))) {
      allComponents.add(new InputComponent(device, Button.DPAD_UP));
      allComponents.add(new InputComponent(device, Button.DPAD_DOWN));
      allComponents.add(new InputComponent(device, Button.DPAD_LEFT));
      allComponents.add(new InputComponent(device, Button.DPAD_RIGHT));
    }

    device.setComponents(allComponents);
  }

  static float[] handlePolledValues(final InputDevice device, final float[] nativeValues) {
    var allValues = new float[device.getComponents().size()];
    var dpadUpIndex = device.getComponentIndex(Button.DPAD_UP);
    var dpadDownIndex = device.getComponentIndex(Button.DPAD_DOWN);
    var dpadLeftIndex = device.getComponentIndex(Button.DPAD_LEFT);
    var dpadRightIndex = device.getComponentIndex(Button.DPAD_RIGHT);

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

      if (component.getId().equals(Axis.AXIS_DPAD)) {
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
   * The `DPAD_VALUE` class defines standard values for IOKit values.
   */
  private static class DPAD_VALUE {
    public static final float UP_LEFT = 8;
    public static final float UP = 1;
    public static final float UP_RIGHT = 2;
    public static final float RIGHT = 3;
    public static final float DOWN_RIGHT = 4;
    public static final float DOWN = 5;
    public static final float DOWN_LEFT = 6;
    public static final float LEFT = 7;
  }
}