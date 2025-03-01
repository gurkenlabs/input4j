package de.gurkenlabs.input4j.foreign.windows.dinput;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DirectInputPluginTests {
  @Test
  @EnabledOnOs(OS.WINDOWS)
  void testInitDirectInputPlugin() {
    assertDoesNotThrow(DirectInputPlugin::new);
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  void testLinuxEventDevicesInit() {
    try (var plugin = new DirectInputPlugin()) {
      plugin.internalInitDevices(null);
    }
  }
}
