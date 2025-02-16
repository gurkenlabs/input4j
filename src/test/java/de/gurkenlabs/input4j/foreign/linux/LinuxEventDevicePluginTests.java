package de.gurkenlabs.input4j.foreign.linux;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinuxEventDevicePluginTests {
  @Test
  void testNormalizeInputValue() {
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
}
