package de.gurkenlabs.input4j;

import java.io.Closeable;
import java.util.Collection;

public interface InputDevicePlugin extends Closeable {

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
  Collection<InputDevice> getAll();
}
