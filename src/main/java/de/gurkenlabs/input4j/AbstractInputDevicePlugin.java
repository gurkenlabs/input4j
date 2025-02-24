package de.gurkenlabs.input4j;


import java.util.logging.Logger;

public abstract class AbstractInputDevicePlugin implements InputDevicePlugin {
  protected static final Logger log = Logger.getLogger(AbstractInputDevicePlugin.class.getPackage().getName());

  protected AbstractInputDevicePlugin() {
  }
}
