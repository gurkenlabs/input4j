package de.gurkenlabs.input4j;

import java.util.logging.Logger;

public final class InputDevices {
  static Logger log = Logger.getLogger(InputDevices.class.getName());

  /**
   * Initializes the input device provider based on the detected operating system.
   *
   * @return The initialized input device provider.
   * @throws Exception if the input device provider cannot be initialized.
   */
  static InputDevicePlugin init() throws Exception {
    String osName = System.getProperty("os.name", "").trim().toLowerCase();
    String osVersion = System.getProperty("os.version");
    String osArch = System.getProperty("os.arch");

    log.fine("Detected OS: " + osName + " Version: " + osVersion + " (" + osArch + ")");

    String pluginClassName = null;
    if (osName.contains("windows")) {
      pluginClassName = Libraries.WIN_DIRECTINPUT.getPlugin();
    } else if (osName.contains("linux")) {
      pluginClassName = Libraries.LINUX_INPUT.getPlugin();
    } else if (osName.contains("mac os")) {
      // TODO: Implement mac support
    }

    if (pluginClassName == null) {
      throw new IllegalStateException("Could not initialize input device provider: Unknown operating system " + osName + " Version: " + osVersion + " (" + osArch + ")");
    }

    var pluginClass = Class.forName(pluginClassName);
    var constructor = pluginClass.getDeclaredConstructor();
    var provider = (InputDevicePlugin) constructor.newInstance();
    provider.internalInitDevices();
    return provider;
  }

  enum Libraries {
    WIN_DIRECTINPUT("de.gurkenlabs.input4j.foreign.windows.dinput.DirectInputPlugin"),
    // WIN_XINPUT("de.gurkenlabs.input4j.foreign.windows.xinput.XInputPlugin");
    LINUX_INPUT("de.gurkenlabs.input4j.foreign.linux.LinuxEventDevicePlugin"),

    // OSX_IOKIT("de.gurkenlabs.input4j.foreign.osx.hid.HumanInterfaceDevicePlugin")
    OSX_GAMECONTROLLER("de.gurkenlabs.input4j.foreign.osx.gc.GameControllerPlugin");

    private final String plugin;

    Libraries(String plugin) {
      this.plugin = plugin;
    }

    public String getPlugin() {
      return plugin;
    }
  }
}
