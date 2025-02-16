package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinuxEventDevicePluginTests {
  @Test
  void testNormalizeInputValueForAxis() {
    // Test case 1: EV_ABS type, value within range
    LinuxEventComponent component = new LinuxEventComponent(LinuxComponentType.ABS_HAT1X, true, false, LinuxEventDevice.EV_ABS, 0x12, -1000, 1000, 0, 10);

    input_event event = new input_event();
    event.value = 500;

    float normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(0.5, normalizedValue, 0.01);

    // Test case 2: EV_ABS type, value at midpoint
    event.value = 0;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(0, normalizedValue, 0.01);

    // Test case 3: EV_ABS type, value at min
    event.value = -1000;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(-1, normalizedValue, 0.01);

    // Test case 4: EV_ABS type, value at max
    event.value = 1000;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);

    // Test case 5: EV_ABS type, value within fuzz range
    event.value = 5;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(0, normalizedValue, 0.01);

    // Test case 6: EV_ABS type, value outside min range
    event.value = -5000;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(-1, normalizedValue, 0.01);

    // Test case 7: EV_ABS type, value outside max range
    event.value = 5000;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);
  }

  @Test
  void testNormalizeInputValueForEVKey() {
    // Test case: EV_KEY type, value is 0
    LinuxEventComponent component = new LinuxEventComponent(LinuxEventDevice.EV_KEY, 0);
    input_event event = new input_event();
    event.value = 0;

    float normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(0, normalizedValue, 0.01);

    // Test case: EV_KEY type, value is non-zero
    event.value = 1;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);

    // Test case: EV_KEY type, value is greater than 1
    event.value = 100;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);

    // Test case: EV_KEY type, value is smaller than -1
    event.value = -100;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);

    // Test case: EV_KEY type, value is negative
    event.value = -1;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);
  }

  @Test
  void testGetComponentIndexByNativeId() {
    // Create a mock InputDevice with components
    InputDevice inputDevice = new InputDevice("Test Device", "Test Device", null, null);
    InputComponent buttonComponent = new InputComponent(inputDevice, new InputComponent.ID(InputComponent.BUTTON_1, 123), "Button1", false);
    InputComponent axisComponent = new InputComponent(inputDevice, new InputComponent.ID(InputComponent.AXIS_X, 456), "Axis1", false);
    inputDevice.addComponent(buttonComponent);
    inputDevice.addComponent(axisComponent);

    // Test case 1: EV_KEY type, matching component
    input_event event = new input_event();
    event.type = LinuxEventDevice.EV_KEY;
    event.code = 123;
    int index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(0, index);

    // Test case 2: EV_ABS type, matching component
    event.type = LinuxEventDevice.EV_ABS;
    event.code = 456;
    index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(1, index);

    // Test case 3: EV_KEY type, no matching component
    event.type = LinuxEventDevice.EV_KEY;
    event.code = 3;
    index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(Linux.ERROR, index);

    // Test case 4: EV_ABS type, no matching component
    event.type = LinuxEventDevice.EV_ABS;
    event.code = 3;
    index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(Linux.ERROR, index);

    // Test case 5: Unknown component type
    InputComponent unknownComponent = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.Unknown, 1111, "Unknown"), "Unknown", false);
    inputDevice.addComponent(unknownComponent);
    event.type = LinuxEventDevice.EV_KEY;
    event.code = 4;
    index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(Linux.ERROR, index);
  }
}
