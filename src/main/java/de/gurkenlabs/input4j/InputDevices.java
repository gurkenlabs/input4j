package de.gurkenlabs.input4j;

import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The `InputDevices` class is responsible for initializing and managing input device providers.
 * It supports different input libraries for various platforms and allows explicit selection of the library.
 */
public final class InputDevices {
  private static final Logger log = Logger.getLogger(InputDevices.class.getName());
  private static final DefaultInputConfiguration configuration = new DefaultInputConfiguration();

  /**
   * This class should not be instantiated.
   */
  private InputDevices() {
  }

  /**
   * Configures the default input settings that are applied to all input devices upon initialization.
   * <p>
   * Make sure to call this method before initializing the input device plugin.
   *
   * @return The default input configuration.
   */
  public static DefaultInputConfiguration configure() {
    return configuration;
  }

  /**
   * Initializes the input device provider with the default platform library.
   *
   * @return The initialized input device provider or null if the initialization fails.
   */
  public static InputDevicePlugin init() {
    return init(null, InputLibrary.PLATFORM_DEFAULT);
  }

  /**
   * Initializes the input device provider with the default platform library and the specified owner.
   *
   * @param owner The owner to be passed to individual plugins, or null if running in background.
   * @return The initialized input device provider or null if the initialization fails.
   */
  public static InputDevicePlugin init(Frame owner) {
    return init(owner, InputLibrary.PLATFORM_DEFAULT);
  }

  /**
   * Initializes the input device provider with the specified library.
   * <p>
   * Note: Some controllers don't support background mode which is why it can be necessary to pass a frame owner to the {@link InputDevices#init(Frame, String)} method.
   *
   * @param library The library to be used.
   * @return The initialized input device provider or null if the initialization fails.
   */
  public static InputDevicePlugin init(InputLibrary library) {
    return init(null, library);
  }

  /**
   * Initializes the input device provider with the specified input plugin class.
   * <p>
   * Note: Some controllers don't support background mode which is why it can be necessary to pass a frame owner to the {@link InputDevices#init(Frame, String)} method.
   *
   * @param inputPluginClass The input plugin class to be used.
   * @return The initialized input device provider or null if the initialization fails.
   */
  public static InputDevicePlugin init(String inputPluginClass) {
    return init(null, inputPluginClass);
  }

  /**
   * Initializes the input device provider based on the detected operating system.
   *
   * @param owner   The owner to be passed to individual plugins, or null if running in background.
   * @param library The input library to be used.
   * @return The initialized input device provider or null if the initialization fails.
   */
  public static InputDevicePlugin init(Frame owner, InputLibrary library) {
    return init(owner, library.getPlugin());
  }

  /**
   * Initializes the input device provider based on the detected operating system.
   *
   * @param owner            The owner to be passed to individual plugins, or null if running in background.
   * @param inputPluginClass The input plugin class to be used. If null, the default platform library is used.
   *                         The plugin class must be a fully qualified class name of a class that implements the {@link InputDevicePlugin} interface.
   *                         <p>
   *                         This can be used to explicitly select a custom input library implementation.
   *                         If the class is not found or cannot be instantiated, an {@link IOException} is thrown.
   *                         The class must have a public no-argument constructor.
   *                         </p>
   * @return The initialized input device provider or null if the initialization fails.
   */
  public static InputDevicePlugin init(Frame owner, String inputPluginClass) {
    try {
      if (inputPluginClass == null) {
        inputPluginClass = InputLibrary.PLATFORM_DEFAULT.getPlugin();
      }

      var pluginClass = Class.forName(inputPluginClass);
      var constructor = pluginClass.getDeclaredConstructor();
      var provider = (InputDevicePlugin) constructor.newInstance();
      provider.internalInitDevices(owner);
      return provider;
    } catch (Exception e) {
      log.log(Level.SEVERE, "Could not initialize input device provider: " + e.getMessage(), e);
      return null;
    }
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
     *
     * <p> Note: when using newer Gamepads or Xbox controllers, consider using {@link #WIN_XINPUT} instead.
     * <p>
     * Some controllers don't support background mode which is why it can be necessary to pass a frame owner to the {@link InputDevices#init(Frame, InputLibrary)} method.
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
     * The IOKIT library for macOS.
     * This library provides support for game controllers on macOS.
     */
    MACOS_IOKIT;

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
        case MACOS_IOKIT -> "de.gurkenlabs.input4j.foreign.macos.iokit.IOKitPlugin";
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

      log.fine("Detected OS: " + osName + " Version: " + osVersion + " (" + osArch + ")");

      String pluginClassName = null;
      if (osName.contains("windows")) {
        pluginClassName = InputLibrary.WIN_XINPUT.getPlugin();
      } else if (osName.contains("linux")) {
        pluginClassName = InputLibrary.LINUX_INPUT.getPlugin();
      } else if (osName.contains("mac os")) {
        pluginClassName = InputLibrary.MACOS_IOKIT.getPlugin();
      }

      if (pluginClassName == null) {
        throw new IllegalStateException("Could not initialize input device provider: Unknown operating system " + osName + " Version: " + osVersion + " (" + osArch + ")");
      }

      return pluginClassName;
    }
  }

  /**
   * Global default configuration settings for all input devices.
   * <p>
   * The settings provided by this class are applied to input devices upon initialization.
   * They cannot be changed in this class at runtime. To adjust settings for individual input devices and plugins, use the
   * correlating methods provided by the input device plugin and input device classes.
   */
  public static final class DefaultInputConfiguration {
    // default number of decimal places to round input data to
    private static final int DEFAULT_ACCURACY = 3;

    private static final int DEFAULT_HOTPLUG_INTERVAL = 3000;

    private int accuracy;

    private int hotplugInterval;

    private DefaultInputConfiguration() {
      this.accuracy = DEFAULT_ACCURACY;
      this.hotplugInterval = DEFAULT_HOTPLUG_INTERVAL;
    }

    /**
     * Gets the number of decimal places to round input data to.
     *
     * @return The number of decimal places.
     */
    public int getAccuracy() {
      return accuracy;
    }

    /**
     * Sets the number of decimal places to round input data to.
     *
     * @param accuracy The number of decimal places. Must be a non-negative integer and should not exceed 7.
     * @throws IllegalArgumentException if the accuracy is negative.
     */
    public void setAccuracy(int accuracy) {
      this.accuracy = accuracy;
    }

    /**
     * Gets the interval in milliseconds for checking for hot-plugged devices.
     * <p>
     * This interval determines how often the system checks for newly connected or disconnected input devices.
     * A shorter interval means more frequent checks, which can lead to faster detection of new devices but may also increase CPU usage.
     * </p>
     * By default, it is set to {@value #DEFAULT_HOTPLUG_INTERVAL} milliseconds.
     * </p>
     *
     * @return The hot-plug interval in milliseconds.
     *
     * @see #DEFAULT_HOTPLUG_INTERVAL
     */
    public int getHotPlugInterval() {
      return hotplugInterval;
    }

    /**
     * Sets the interval in milliseconds for checking for hot-plugged devices.
     * <p>
     * This interval determines how often the system checks for newly connected or disconnected input devices.
     * A shorter interval means more frequent checks, which can lead to faster detection of new devices but may also increase CPU usage.
     * </p>
     *
     * @param hotplugInterval The hot-plug interval in milliseconds.
     * @throws IllegalArgumentException if the interval is negative.
     */
    public void setHotPlugInterval(int hotplugInterval) {
      this.hotplugInterval = hotplugInterval;
    }
  }
}
