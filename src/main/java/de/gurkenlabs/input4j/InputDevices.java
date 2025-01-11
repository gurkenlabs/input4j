package de.gurkenlabs.input4j;

import java.awt.*;
import java.util.logging.Logger;

/**
 * The `InputDevices` class is responsible for initializing and managing input device providers.
 * It supports different input libraries for various platforms and allows explicit selection of the library.
 */
public final class InputDevices {
  static Logger log = Logger.getLogger(InputDevices.class.getName());

  /**
   * Initializes the input device provider with the default platform library.
   *
   * @return The initialized input device provider.
   * @throws Exception if the input device provider cannot be initialized.
   */
  static InputDevicePlugin init() throws Exception {
    return init(null, InputLibrary.PLATFORM_DEFAULT);
  }

  /**
   * Initializes the input device provider with the default platform library and the specified owner.
   *
   * @param owner The owner to be passed to individual plugins, or null if running in background.
   * @return The initialized input device provider.
   * @throws Exception if the input device provider cannot be initialized.
   */
  static InputDevicePlugin init(Frame owner) throws Exception {
    return init(owner, InputLibrary.PLATFORM_DEFAULT);
  }

  /**
   * Initializes the input device provider with the specified library.
   *
   * @param library The library to be used.
   * @return The initialized input device provider.
   * @throws Exception if the input device provider cannot be initialized.
   */
  static InputDevicePlugin init(InputLibrary library) throws Exception {
    return init(null, library);
  }

  /**
   * Initializes the input device provider based on the detected operating system.
   *
   * @param owner The owner to be passed to individual plugins, or null if running in background.
   * @return The initialized input device provider.
   * @throws Exception if the input device provider cannot be initialized.
   */
  static InputDevicePlugin init(Frame owner, InputLibrary library) throws Exception {
    var pluginClass = Class.forName(library.getPlugin());
    var constructor = pluginClass.getDeclaredConstructor();
    var provider = (InputDevicePlugin) constructor.newInstance();
    provider.internalInitDevices(owner);
    return provider;
  }

  /**
   * Enum representing the available input libraries.
   * This enum provides a list of supported input libraries for different platforms.
   * Each enum constant corresponds to a specific input library implementation.
   */
  public enum InputLibrary {
    /**
     * The default input library for the current platform.
     * This will be determined based on the operating system at runtime.
     */
    PLATFORM_DEFAULT,

    /**
     * The DirectInput library for Windows.
     * This library provides support for older input devices on Windows.
     */
    WIN_DIRECTINPUT,

    /**
     * The XInput library for Windows.
     * This library provides support for Xbox controllers on Windows.
     */
    WIN_XINPUT,

    /**
     * The input library for Linux.
     * This library provides support for input devices on Linux.
     */
    LINUX_INPUT,

    /**
     * The GameController library for macOS.
     * This library provides support for game controllers on macOS.
     */
    OSX_GAMECONTROLLER;

    /**
     * Gets the plugin class name for the specified library.
     *
     * @return The plugin class name.
     */
    private String getPlugin() {
      return switch (this) {
        case PLATFORM_DEFAULT -> getCurrentPlatformDefaultPlugin();
        case WIN_DIRECTINPUT -> "de.gurkenlabs.input4j.foreign.windows.dinput.DirectInputPlugin";
        case WIN_XINPUT -> "de.gurkenlabs.input4j.foreign.windows.xinput.XInputPlugin";
        case LINUX_INPUT -> "de.gurkenlabs.input4j.foreign.linux.LinuxEventDevicePlugin";
        case OSX_GAMECONTROLLER -> "de.gurkenlabs.input4j.foreign.osx.gc.GameControllerPlugin";
      };
    }

    /**
     * Detects the current platform and returns the default plugin class name for the platform.
     *
     * @return The default plugin class name for the current platform.
     */
    private String getCurrentPlatformDefaultPlugin() {
      String osName = System.getProperty("os.name", "").trim().toLowerCase();
      String osVersion = System.getProperty("os.version");
      String osArch = System.getProperty("os.arch");

      log.warning("Detected OS: " + osName + " Version: " + osVersion + " (" + osArch + ")");

      String pluginClassName = null;
      if (osName.contains("windows")) {
        pluginClassName = InputLibrary.WIN_XINPUT.getPlugin();
      } else if (osName.contains("linux")) {
        pluginClassName = InputLibrary.LINUX_INPUT.getPlugin();
      } else if (osName.contains("mac os")) {
        // TODO: Implement mac support
      }

      if (pluginClassName == null) {
        throw new IllegalStateException("Could not initialize input device provider: Unknown operating system " + osName + " Version: " + osVersion + " (" + osArch + ")");
      }

      return pluginClassName;
    }
  }
}
