package de.gurkenlabs.litiengine.input;

import java.util.Collection;

public interface InputDeviceProvider {
  void collectDevices();

  Collection<InputDevice> getDevices();
}
