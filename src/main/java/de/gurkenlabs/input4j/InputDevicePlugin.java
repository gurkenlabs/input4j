package de.gurkenlabs.input4j;

import java.awt.*;
import java.io.Closeable;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Represents a plugin that provides input devices.
 * <p>
 * This interface is used to define a plugin that provides input devices for the {@link InputDevices} class.
 * The plugin is responsible for initializing the input devices and providing them to the input device manager.
 * </p>
 *
 * @see InputDevices
 */
public interface InputDevicePlugin extends Closeable {

  /**
   * Initializes the input devices for the plugin.
   * <p>
   * This method is called internally when initializing the {@link InputDevices}.
   * It sets up the necessary input devices based on the provided owner frame.
   * If the owner frame is null, the input devices are initialized to run in the background (if supported by the plugin).
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

  /**
   * Registers a listener to be notified when the list of input devices changes.
   * <p>
   * This method allows you to register a {@link Runnable} that will be executed whenever the list of input devices changes.
   * This can happen when a new device is connected or an existing device is disconnected.
   * <p>
   *   This is useful for updating the UI or other parts of the application that depend on the list of input devices.
   *   The listener will be called on the same thread that called the {@link InputDevice#poll()} method.
   *   <p>
   *   The list of input devices is refreshed periodically based on the {@link InputDevices.DefaultInputConfiguration#getHotPlugInterval()} setting.
   * </p>
   *
   * @param listener The listener to register.
   */
  void onDevicesChanged(Runnable listener);

  /**
   * Registers a listener to be notified when a new input device is connected.
   * <p>
   * This method allows you to register a {@link Consumer} that will be executed whenever a new input device is connected.
   * This can happen when a new device is plugged in or when a device that was previously disconnected is reconnected.
   * <p>
   *   This is useful for updating the UI or other parts of the application that depend on the list of input devices.
   *   The listener will be called on the same thread that called the {@link InputDevice#poll()} method.
   *   <p>
   *   The list of input devices is refreshed periodically based on the {@link InputDevices.DefaultInputConfiguration#getHotPlugInterval()} setting.
   * </p>
   *
   * @param listener The listener to register.
   */
  void onDeviceConnected(Consumer<InputDevice> listener);

  /**
   * Registers a listener to be notified when an input device is disconnected.
   * <p>
   * This method allows you to register a {@link Consumer} that will be executed whenever an input device is disconnected.
   * This can happen when a device is unplugged or when a device that was previously connected is disconnected.
   * <p>
   *   This is useful for updating the UI or other parts of the application that depend on the list of input devices.
   *   The listener will be called on the same thread that called the {@link InputDevice#poll()} method.
   *   <p>
   *   The list of input devices is refreshed periodically based on the {@link InputDevices.DefaultInputConfiguration#getHotPlugInterval()} setting.
   * </p>
   *
   * @param listener The listener to register.
   */
  void onDeviceDisconnected(Consumer<InputDevice> listener);
}
