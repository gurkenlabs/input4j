package de.gurkenlabs.input4j.foreign.macos.iokit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IOKitPluginTests {
  @Test
  void testNormalizeInputValue() {
    IOHIDElement element = new IOHIDElement();
    element.min = 0;
    element.max = 100;
    element.usage = IOHIDElementUsage.X;
    element.type = IOHIDElementType.AXIS;

    // Test normalization for axis
    assertEquals(0.0f, IOKitPlugin.normalizeInputValue(50, element, true), 0.01);
    assertEquals(0.0f, IOKitPlugin.normalizeInputValue(0, element, true), 0.01);
    assertEquals(-0.98f, IOKitPlugin.normalizeInputValue(1, element, true), 0.01);
    assertEquals(1.0f, IOKitPlugin.normalizeInputValue(100, element, true), 0.01);

    // Test normalization for button
    element.type = IOHIDElementType.BUTTON;
    assertEquals(0.0f, IOKitPlugin.normalizeInputValue(0, element, false), 0.01);
    assertEquals(1.0f, IOKitPlugin.normalizeInputValue(1, element, false), 0.01);
    assertEquals(1.0f, IOKitPlugin.normalizeInputValue(100, element, false), 0.01);
  }
}
