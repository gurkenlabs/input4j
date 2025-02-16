package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;

import java.util.ArrayList;
import java.util.Collection;

final class LinuxVirtualComponentHandler {
  private LinuxVirtualComponentHandler() {
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

    // in Linux, the D-Pad is mapped to two buttons for LEFT/RIGHT and UP/DOWN respectively.
    // We need separate button components that reflect the D-Pad directions individually
    if (components.stream().anyMatch(x -> x.getId().name.equals(LinuxEventComponent.ID_DPAD_UP_DOWN))) {
      allComponents.add(new InputComponent(device, InputComponent.DPAD_UP));
      allComponents.add(new InputComponent(device, InputComponent.DPAD_DOWN));
    }

    if (components.stream().anyMatch(x -> x.getId().name.equals(LinuxEventComponent.ID_DPAD_LEFT_RIGHT))) {
      allComponents.add(new InputComponent(device, InputComponent.DPAD_LEFT));
      allComponents.add(new InputComponent(device, InputComponent.DPAD_RIGHT));
    }

    device.setComponents(allComponents);
  }

  static float[] handlePolledValues(final InputDevice device, final float[] nativeValues) {
    var allValues = new float[device.getComponents().size()];
    var dpadUpIndex = device.getComponentIndex(InputComponent.DPAD_UP);
    var dpadDownIndex = device.getComponentIndex(InputComponent.DPAD_DOWN);
    var dpadLeftIndex = device.getComponentIndex(InputComponent.DPAD_LEFT);
    var dpadRightIndex = device.getComponentIndex(InputComponent.DPAD_RIGHT);

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

      if (component.getId().name.equals(LinuxEventComponent.ID_DPAD_UP_DOWN)) {
        allValues[i] = 0;

        if (value == DPAD_VALUE.UP) {
          if (dpadUpIndex != -1) allValues[dpadUpIndex] = 1;
        } else if (value == DPAD_VALUE.DOWN) {
          if (dpadDownIndex != -1) allValues[dpadDownIndex] = 1;
        } else if (value == 0) {
          if (dpadUpIndex != -1) allValues[dpadUpIndex] = 0;
          if (dpadDownIndex != -1) allValues[dpadDownIndex] = 0;
        }
      }

      if (component.getId().name.equals(LinuxEventComponent.ID_DPAD_LEFT_RIGHT)) {
        allValues[i] = 0;

        if (value == DPAD_VALUE.LEFT) {
          if (dpadLeftIndex != -1) allValues[dpadLeftIndex] = 1;
        } else if (value == DPAD_VALUE.RIGHT) {
          if (dpadRightIndex != -1) allValues[dpadRightIndex] = 1;
        } else if (value == 0) {
          if (dpadLeftIndex != -1) allValues[dpadLeftIndex] = 0;
          if (dpadRightIndex != -1) allValues[dpadRightIndex] = 0;
        }
      }
    }

    return allValues;
  }

  private static class DPAD_VALUE {
    public static final float UP = 1f;
    public static final float DOWN = -1f;
    public static final float LEFT = 1f;
    public static final float RIGHT = -1f;
  }
}
