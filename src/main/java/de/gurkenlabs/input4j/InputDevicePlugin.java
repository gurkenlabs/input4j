package de.gurkenlabs.input4j;

import java.awt.*;
import java.io.Closeable;
import java.util.Collection;

public interface InputDevicePlugin extends Closeable {

  /**
   * Initializes the input devices for the plugin.
   * <p>
   * This method is called internally when initializing the {@link InputDevices}.
   * It sets up the necessary input devices based on the provided owner frame.
   * If the owner frame is null, the input devices are initialized to run in the background.
   * </p>
   *
   * @param owner The frame owner to be passed to individual plugins, or null if running in the background.
   * @see InputDevices#init(Frame)
   */
  void internalInitDevices(Frame owner);

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
