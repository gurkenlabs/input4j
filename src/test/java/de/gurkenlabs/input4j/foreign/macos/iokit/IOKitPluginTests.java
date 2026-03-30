package de.gurkenlabs.input4j.foreign.macos.iokit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void testRumbleConstants() {
    // Test that the rumble report type constants are defined correctly
    // kIOHIDReportTypeOutput = 1 for sending rumble/haptic feedback
    assertEquals(1, MacOS.kIOHIDReportTypeOutput);

    // kIOHIDReportTypeInput = 0 for receiving input
    assertEquals(0, MacOS.kIOHIDReportTypeInput);

    // kIOHIDReportTypeFeature = 2 for feature reports
    assertEquals(2, MacOS.kIOHIDReportTypeFeature);
  }

  @Test
  void testIntensityToBytes() {
    // Test the actual implementation method
    // Edge cases
    int[] result = IOKitPlugin.intensityToBytes(0.0f, 0.0f);
    assertEquals(0, result[0]);
    assertEquals(0, result[1]);

    result = IOKitPlugin.intensityToBytes(1.0f, 1.0f);
    assertEquals(255, result[0]);
    assertEquals(255, result[1]);

    // Test mid-range values
    result = IOKitPlugin.intensityToBytes(0.5f, 0.5f);
    assertEquals(127, result[0]);
    assertEquals(127, result[1]);

    // Test values above 1.0 are clamped
    result = IOKitPlugin.intensityToBytes(1.5f, 1.5f);
    assertEquals(255, result[0]);
    assertEquals(255, result[1]);

    // Test negative values are clamped
    result = IOKitPlugin.intensityToBytes(-0.5f, -0.5f);
    assertEquals(0, result[0]);
    assertEquals(0, result[1]);

    // Test asymmetric (left vs right)
    result = IOKitPlugin.intensityToBytes(0.8f, 0.2f);
    assertEquals(204, result[0]);
    assertEquals(51, result[1]);
  }

  @Test
  void testShouldStopRumble() {
    // Test the actual implementation method
    // Null or empty should stop
    assertTrue(IOKitPlugin.shouldStopRumble(null));
    assertTrue(IOKitPlugin.shouldStopRumble(new float[0]));

    // Values below threshold should stop
    assertTrue(IOKitPlugin.shouldStopRumble(new float[] {0.009f}));
    assertTrue(IOKitPlugin.shouldStopRumble(new float[] {0.005f, 0.005f}));

    // Values at or above threshold should NOT stop
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.01f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.5f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.5f, 0.0f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.5f, 0.005f}));

    // When left is below threshold, stop regardless of right (right is never checked)
    // Note: both must be below threshold to stop when there are 2 values
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.005f, 0.5f}));
  }

  @Test
  void testRumbleThreshold() {
    // Test the threshold behavior through the actual implementation
    // Values below threshold should trigger stop rumble
    assertTrue(IOKitPlugin.shouldStopRumble(new float[] {0.005f}));
    assertTrue(IOKitPlugin.shouldStopRumble(new float[] {0.009f}));

    // Values at or above threshold should NOT trigger stop
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.01f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.5f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {1.0f}));
  }
}
