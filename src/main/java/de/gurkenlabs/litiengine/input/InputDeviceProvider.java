package de.gurkenlabs.litiengine.input;

import java.io.Closeable;
import java.util.Collection;
import java.util.logging.Logger;

public interface InputDeviceProvider extends Closeable {
  Logger log = Logger.getLogger(InputDeviceProvider.class.getName());

  void collectDevices();

  Collection<InputDevice> getDevices();

  static InputDeviceProvider init() throws Exception {
    String osName = System.getProperty("os.name", "").trim().toLowerCase();
    String osVersion = System.getProperty("os.version");
    String osArch = System.getProperty("os.arch");

    log.fine("Detected OS: " + osName + " Version: " + osVersion + " (" + osArch + ")");

    String deviceProviderClassName = null;
    if (osName.contains("windows")) {
      deviceProviderClassName = "de.gurkenlabs.litiengine.input.windows.DirectInputDeviceProvider";
    } else if (osName.contains("linux")) {

    } else if (osName.contains("mac os")) {

    }

    if (deviceProviderClassName == null) {
      throw new IllegalStateException("Could not initialize input device provider: Unknown operating system " + osName + " Version: " + osVersion + " (" + osArch + ")");
    }

    var deviceProviderClass = Class.forName(deviceProviderClassName);
    var constructor = deviceProviderClass.getDeclaredConstructor();
    return (InputDeviceProvider) constructor.newInstance();
  }
}
