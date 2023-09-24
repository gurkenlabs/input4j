package de.gurkenlabs.input4j.linux;

import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDevicePlugin;
import de.gurkenlabs.input4j.InputDevices;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;


public class LinuxJoystickPlugin implements InputDevicePlugin {
  private static final Logger log = Logger.getLogger(LinuxJoystickPlugin.class.getName());

  static {
    System.loadLibrary("input");
  }
  @Override
  public void internalInitDevices() {

  }

  @Override
  public Collection<InputDevice> getAll() {
    return null;
  }

  @Override
  public void close() throws IOException {

  }
}
