package de.gurkenlabs.input4j.foreign.util;

import java.util.logging.Logger;

/**
 * Utility class for detecting the current operating system and mapping it to the appropriate
 * {@link de.gurkenlabs.input4j.InputDevices.InputLibrary} enum constant.
 *
 * <p>This class centralises all {@code System.getProperty("os.*")} calls, making the detection
 * logic easy to mock in unit tests and keeping {@link de.gurkenlabs.input4j.InputDevices.InputLibrary}
 * free of side‑effects.</p>
 */
public final class PlatformDetector {
  private static final Logger log = Logger.getLogger(PlatformDetector.class.getName());

  private PlatformDetector() {
    // Utility class – prevent instantiation
  }

  /**
   * Detects the current operating system and returns the corresponding {@link de.gurkenlabs.input4j.InputDevices.InputLibrary}
   * enum constant.
   *
   * @return the detected {@code InputLibrary} for the current OS
   * @throws IllegalStateException if the OS cannot be mapped to a known library
   */
  public static de.gurkenlabs.input4j.InputDevices.InputLibrary detect() {
    String osName = System.getProperty("os.name", "").trim().toLowerCase();
    String osVersion = System.getProperty("os.version");
    String osArch = System.getProperty("os.arch");
    log.fine("Detected OS: " + osName + " Version: " + osVersion + " (" + osArch + ")");

    if (osName.contains("windows")) {
      return de.gurkenlabs.input4j.InputDevices.InputLibrary.WIN_XINPUT;
    } else if (osName.contains("linux")) {
      return de.gurkenlabs.input4j.InputDevices.InputLibrary.LINUX_INPUT;
    } else if (osName.contains("mac os")) {
      return de.gurkenlabs.input4j.InputDevices.InputLibrary.MACOS_IOKIT;
    }
    throw new IllegalStateException(
        "Unknown operating system " + osName + " Version: " + osVersion + " (" + osArch + ")");
  }
}
