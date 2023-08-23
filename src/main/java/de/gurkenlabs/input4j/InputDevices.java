package de.gurkenlabs.input4j;

import java.io.Closeable;
import java.util.Collection;
import java.util.logging.Logger;

public interface InputDevices extends Closeable {
  Logger log = Logger.getLogger(InputDevices.class.getName());

  /**
   * This is called internally when calling initializing the {@link InputDevices }.
   *
   * @see InputDevices#init()
   */
  void internalInitDevices();

  /**
   * Retrieves a collection of input devices.
   * <p>
   * This method returns a collection of {@link InputDevice} objects representing the input devices
   * available on the system. Input devices include gamepads, joysticks, analog keyboards
   * and any other hardware devices that can be used to input data or control the system.
   *
   * @return A collection of {@link InputDevice} objects representing the available input devices of this system.
   */
  Collection<InputDevice> getDevices();

  /**
   * Initializes the input device provider based on the detected operating system.
   *
   * @return The initialized input device provider.
   * @throws Exception if the input device provider cannot be initialized.
   */
  static InputDevices init() throws Exception {
    String osName = System.getProperty("os.name", "").trim().toLowerCase();
    String osVersion = System.getProperty("os.version");
    String osArch = System.getProperty("os.arch");

    log.fine("Detected OS: " + osName + " Version: " + osVersion + " (" + osArch + ")");

    String deviceProviderClassName = null;
    if (osName.contains("windows")) {
      deviceProviderClassName = Libraries.WIN_DIRECTINPUT.getDeviceProvider();
    } else if (osName.contains("linux")) {
      // TODO: Implement linux support
    } else if (osName.contains("mac os")) {
      // TODO: Implement mac support
    }

    if (deviceProviderClassName == null) {
      throw new IllegalStateException("Could not initialize input device provider: Unknown operating system " + osName + " Version: " + osVersion + " (" + osArch + ")");
    }

    var deviceProviderClass = Class.forName(deviceProviderClassName);
    var constructor = deviceProviderClass.getDeclaredConstructor();
    var provider = (InputDevices) constructor.newInstance();
    provider.internalInitDevices();
    return provider;
  }

  enum Libraries {
    WIN_DIRECTINPUT("de.gurkenlabs.input4j.windows.dinput.DirectInputDeviceProvider"),
    // WIN_XINPUT("de.gurkenlabs.input4j.windows.xinput.XInputDeviceProvider");
    LINUX_INPUT("de.gurkenlabs.input4j.linux.JoystickProvider"),

    // OSX_IOKIT("de.gurkenlabs.input4j.osx.hid.HumanInterfaceDeviceProvider")
    OSX_GAMECONTROLLER("de.gurkenlabs.input4j.osx.gc.GameControllerProvider");

    private final String deviceProvider;

    Libraries(String deviceProvider) {
      this.deviceProvider = deviceProvider;
    }

    public String getDeviceProvider() {
      return deviceProvider;
    }
  }
}
