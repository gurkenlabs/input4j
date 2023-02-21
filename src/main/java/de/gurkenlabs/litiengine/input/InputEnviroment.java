package de.gurkenlabs.litiengine.input;

import java.util.logging.Logger;

public final class InputEnviroment {
  private static final Logger log = Logger.getLogger(InputEnviroment.class.getName());

  private static InputDeviceProvider deviceProvider;

  public static void init() throws Exception {
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
      throw new IllegalStateException("Unknown operating system. Could not initialize input device provider.");
    }

    // load device provider via reflection to prevent a hard reference to the platform native implementations
    var deviceProviderClass = Class.forName(deviceProviderClassName);
    var constructor = deviceProviderClass.getDeclaredConstructor();
    constructor.setAccessible(true);
    deviceProvider = (InputDeviceProvider) constructor.newInstance();

    deviceProvider.collectDevices();
  }

}
