package de.gurkenlabs.input4j.foreign.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PlatformDetector}.
 */
class PlatformDetectorTests {

  @BeforeEach
  void setUp() {
    // Reset any system property changes after each test
  }

  @AfterEach
  void tearDown() {
    // Ensure we restore the original system properties
    System.clearProperty("os.name");
    System.clearProperty("os.version");
    System.clearProperty("os.arch");
  }

  @Test
  void testDetect_Windows() {
    System.setProperty("os.name", "Windows 10");
    System.setProperty("os.version", "10.0");
    System.setProperty("os.arch", "x86_64");

    de.gurkenlabs.input4j.InputDevices.InputLibrary lib = de.gurkenlabs.input4j.foreign.util.PlatformDetector.detect();
    assertEquals(de.gurkenlabs.input4j.InputDevices.InputLibrary.WIN_XINPUT, lib);
  }

  @Test
  void testDetect_Linux() {
    System.setProperty("os.name", "Linux");
    System.setProperty("os.version", "5.15.0-76-generic");
    System.setProperty("os.arch", "x86_64");

    de.gurkenlabs.input4j.InputDevices.InputLibrary lib = de.gurkenlabs.input4j.foreign.util.PlatformDetector.detect();
    assertEquals(de.gurkenlabs.input4j.InputDevices.InputLibrary.LINUX_INPUT, lib);
  }

  @Test
  void testDetect_macOS() {
    System.setProperty("os.name", "Mac OS X");
    System.setProperty("os.version", "13.0");
    System.setProperty("os.arch", "x86_64");

    de.gurkenlabs.input4j.InputDevices.InputLibrary lib = de.gurkenlabs.input4j.foreign.util.PlatformDetector.detect();
    assertEquals(de.gurkenlabs.input4j.InputDevices.InputLibrary.MACOS_IOKIT, lib);
  }

  @Test
  void testDetect_UnknownOS() {
    System.setProperty("os.name", "Solaris");
    System.setProperty("os.version", "11");
    System.setProperty("os.arch", "sparc");

    assertThrows(IllegalStateException.class, () -> {
      de.gurkenlabs.input4j.foreign.util.PlatformDetector.detect();
    });
  }

  @Test
  void testDetect_EmptyOSName() {
    System.setProperty("os.name", "");
    System.setProperty("os.version", "1.0");
    System.setProperty("os.arch", "x86");

    assertThrows(IllegalStateException.class, () -> {
      de.gurkenlabs.input4j.foreign.util.PlatformDetector.detect();
    });
  }

  @Test
  void testDetect_NullOSName() {
    System.clearProperty("os.name");
    System.setProperty("os.version", "1.0");
    System.setProperty("os.arch", "x86");

    assertThrows(IllegalStateException.class, () -> {
      de.gurkenlabs.input4j.foreign.util.PlatformDetector.detect();
    });
  }

  @Test
  void testDetect_CaseInsensitive() {
    System.setProperty("os.name", "windows 11");
    System.setProperty("os.version", "10.0.22000");
    System.setProperty("os.arch", "x64");

    de.gurkenlabs.input4j.InputDevices.InputLibrary lib = de.gurkenlabs.input4j.foreign.util.PlatformDetector.detect();
    assertEquals(de.gurkenlabs.input4j.InputDevices.InputLibrary.WIN_XINPUT, lib);
  }
}