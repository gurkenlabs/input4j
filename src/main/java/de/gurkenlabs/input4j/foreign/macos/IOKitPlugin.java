package de.gurkenlabs.input4j.foreign.macos;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.InputDevice;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class IOKitPlugin extends AbstractInputDevicePlugin {

  @Override
  public void internalInitDevices(Frame owner) {

  }

  @Override
  public Collection<InputDevice> getAll() {
    return List.of();
  }

  @Override
  public void close() {

  }
}
