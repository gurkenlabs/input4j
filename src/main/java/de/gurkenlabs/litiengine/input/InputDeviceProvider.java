package de.gurkenlabs.litiengine.input;

import java.io.Closeable;
import java.util.Collection;
import java.util.logging.Logger;

public interface InputDeviceProvider extends Closeable {
  Logger log = Logger.getLogger(InputDeviceProvider.class.getName());

  /**
   * This is called internally when calling initializing the {@link  InputDeviceProvider }.
   *
   * @see InputDeviceProvider#init()
   */
  void initDevices();

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
  static InputDeviceProvider init() throws Exception {
    String osName = System.getProperty("os.name", "").trim().toLowerCase();
    String osVersion = System.getProperty("os.version");
    String osArch = System.getProperty("os.arch");

    log.fine("Detected OS: " + osName + " Version: " + osVersion + " (" + osArch + ")");

    String deviceProviderClassName = null;
    if (osName.contains("windows")) {
      deviceProviderClassName = "de.gurkenlabs.litiengine.input.windows.DirectInputDeviceProvider";
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
    var provider = (InputDeviceProvider) constructor.newInstance();
    provider.initDevices();
    return provider;
  }
}
