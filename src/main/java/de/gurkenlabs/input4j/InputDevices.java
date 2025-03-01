package de.gurkenlabs.input4j;

import java.awt.*;
import java.io.IOException;
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
   * @return The initialized input device provider.
   * @throws IOException if the input device provider cannot be initialized.
   */
  public static InputDevicePlugin init() throws IOException {
    return init(null, InputLibrary.PLATFORM_DEFAULT);
  }

  /**
   * Initializes the input device provider with the default platform library and the specified owner.
   *
   * @param owner The owner to be passed to individual plugins, or null if running in background.
   * @return The initialized input device provider.
   * @throws IOException if the input device provider cannot be initialized.
   */
  public static InputDevicePlugin init(Frame owner) throws IOException {
    return init(owner, InputLibrary.PLATFORM_DEFAULT);
  }

  /**
   * Initializes the input device provider with the specified library.
   * <p>
   *   Note: Some controllers don't support background mode which is why it can be necessary to pass a frame owner to the {@link InputDevices#init(Frame, String)} method.
   * @param library The library to be used.
   * @return The initialized input device provider.
   * @throws IOException if the input device provider cannot be initialized.
   */
  public static InputDevicePlugin init(InputLibrary library) throws IOException {
    return init(null, library);
  }

  /**
   * Initializes the input device provider with the specified input plugin class.
   * <p>
   *   Note: Some controllers don't support background mode which is why it can be necessary to pass a frame owner to the {@link InputDevices#init(Frame, String)} method.
   * @param inputPluginClass The input plugin class to be used.
   * @return The initialized input device provider.
   * @throws IOException if the input device provider cannot be initialized.
   */
  public static InputDevicePlugin init(String inputPluginClass) throws IOException {
    return init(null, inputPluginClass);
  }

  /**
   * Initializes the input device provider based on the detected operating system.
   *
   * @param owner   The owner to be passed to individual plugins, or null if running in background.
   * @param library The input library to be used.
   * @return The initialized input device provider.
   * @throws IOException if the input device provider cannot be initialized.
   */
  public static InputDevicePlugin init(Frame owner, InputLibrary library) throws IOException {
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
   * @return The initialized input device provider.
   * @throws IOException if the input device provider cannot be initialized.
   */
  public static InputDevicePlugin init(Frame owner, String inputPluginClass) throws IOException {
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
      throw new IOException("Could not initialize input device provider: " + e.getMessage(), e);
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

    // default polling rate in hertz (times per second)
    private static final int DEFAULT_POLLING_RATE = 100;

    private int pollingRate;
    private boolean pollingEnabled;

    private int accuracy;

    private DefaultInputConfiguration() {
      this.pollingRate = DEFAULT_POLLING_RATE;
      this.pollingEnabled = false;

      this.accuracy = DEFAULT_ACCURACY;
    }

    /**
     * Gets the polling rate in hertz (times per second).
     *
     * @return The polling rate.
     */
    public int getPollingRate() {
      return pollingRate;
    }

    /**
     * Sets the polling rate in hertz (times per second).
     *
     * @param pollingRate The polling rate.
     */
    public void setPollingRate(int pollingRate) {
      this.pollingRate = pollingRate;
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
     * Checks if polling is enabled.
     *
     * @return {@code true} if polling is enabled, {@code false} otherwise.
     */
    public boolean isPollingEnabled() {
      return pollingEnabled;
    }

    /**
     * Sets whether polling is enabled.
     *
     * @param pollingEnabled {@code true} to enable automatic polling at the interval defined by the polling rate,
     *                       {@code false} to disable automatic polling and require manual polling via {@link InputDevice#poll()}.
     *                       <p>
     *                       The polling rate determines how frequently the input device is polled for new data when automatic polling is enabled.
     *                       It is measured in hertz. A higher polling rate means the device is checked more frequently,
     *                       which can lead to more responsive input handling but may also increase CPU usage.
     */
    public void enablePolling(boolean pollingEnabled) {
      this.pollingEnabled = pollingEnabled;
    }
  }
}
