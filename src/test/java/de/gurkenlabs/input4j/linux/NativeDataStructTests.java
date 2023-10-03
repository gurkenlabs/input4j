package de.gurkenlabs.input4j.linux;

import de.gurkenlabs.input4j.windows.dinput.DirectInputPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledOnOs(OS.LINUX)
public class NativeDataStructTests {
  @Test
  void testInitLinuxEventDevicePlugin() {
    assertDoesNotThrow(DirectInputPlugin::new);
  }
}
