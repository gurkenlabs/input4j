package de.gurkenlabs.input4j.foreign.windows.xinput;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class XInputPluginTests {
  @Test
  @EnabledOnOs(OS.WINDOWS)
  void testInitXInputPlugin() {
    assertDoesNotThrow(XInputPlugin::new);
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void testLinuxEventDevicesInit() {
    try (var plugin = new XInputPlugin()) {
      plugin.internalInitDevices(null);
    }
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void testNormalizeTrigger() {
    // Test case 1: Value below threshold
    assertEquals(0.0f, XInputPlugin.normalizeTrigger((byte) 10), 0.01);

    // Test case 2: Value at threshold
    assertEquals(0.117f, XInputPlugin.normalizeTrigger((byte) XINPUT_GAMEPAD.XINPUT_GAMEPAD_TRIGGER_THRESHOLD), 0.01);

    // Test case 3: Value above threshold
    assertEquals(0.5f, XInputPlugin.normalizeTrigger((byte) 128), 0.01);

    // Test case 4: Maximum value
    assertEquals(1.0f, XInputPlugin.normalizeTrigger((byte) 255), 0.01);
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void testNormalizeSignedShort() {
    // Test case 1: Value within deadzone
    assertEquals(0.0f, XInputPlugin.normalizeSignedShort((short) 500, 1000), 0.01);

    // Test case 2: Value at deadzone boundary
    assertEquals(0.0305f, XInputPlugin.normalizeSignedShort((short) 1000, 1000), 0.01);

    // Test case 3: Value outside deadzone
    assertEquals(0.5f, XInputPlugin.normalizeSignedShort((short) 16384, 1000), 0.01);

    // Test case 4: Value is Short.MIN_VALUE
    assertEquals(-1.0f, XInputPlugin.normalizeSignedShort(Short.MIN_VALUE, 1000), 0.01);

    // Test case 5: Value is Short.MAX_VALUE
    assertEquals(1.0f, XInputPlugin.normalizeSignedShort(Short.MAX_VALUE, 1000), 0.01);

    // Test case 6: Negative value within deadzone
    assertEquals(0.0f, XInputPlugin.normalizeSignedShort((short) -500, 1000), 0.01);

    // Test case 7: Negative value outside deadzone
    assertEquals(-0.5f, XInputPlugin.normalizeSignedShort((short) -16384, 1000), 0.01);
  }
}
