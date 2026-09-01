package de.gurkenlabs.input4j;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Base class for input device plugins that provides common functionality for device management,
 * hot-plugging support, and event handling.
 */
public abstract class AbstractInputDevicePlugin implements InputDevicePlugin {
  /** Logger for the plugin package. */
  protected static final Logger log = Logger.getLogger(AbstractInputDevicePlugin.class.getPackage().getName());
  private final Collection<Consumer<InputDevice>> deviceConnectedListeners = ConcurrentHashMap.newKeySet();
  private final Collection<Consumer<InputDevice>> deviceDisconnectedListeners = ConcurrentHashMap.newKeySet();
  private final Collection<Runnable> devicesChangedListeners = ConcurrentHashMap.newKeySet();

  private final int hotPlugInterval;
  private long lastDeviceUpdate;
  private Collection<InputDevice> devices;

  /**
   * Initializes the plugin with the hot-plug interval from the configuration.
   */
  protected AbstractInputDevicePlugin() {
    this.hotPlugInterval = InputDevices.configure().getHotPlugInterval();
  }

  /**
   * Gets all devices that are managed by this plugin. If the plugin has not been initialized yet, this method will throw an {@code IllegalStateException}.
   *
   * @return A collection of all devices that are managed by this plugin.
   */
  @Override
  public Collection<InputDevice> getAll() {
    if (this.devices == null) {
      throw new IllegalStateException("The plugin has not been initialized yet.");
    }

    return this.devices;
  }

  /**
   * Closes the plugin and clears the collection of devices.
   */
  @Override
  public void close() {
    if (this.devices != null) {
      this.devices.forEach(InputDevice::close);
    }

    deviceConnectedListeners.clear();
    deviceDisconnectedListeners.clear();
    devicesChangedListeners.clear();
  }

  /**
   * Sets the devices that are managed by this plugin.
   * <p>
   * <b>IMPORTANT</b>: This method needs to be called by the implementing class to set the devices that are managed by this plugin.
   *
   * @param devices The devices to set.
   */
  protected void setDevices(Collection<InputDevice> devices) {
    this.devices = devices;
    this.lastDeviceUpdate = System.currentTimeMillis();
  }

  /**
   * Refreshes the list of input devices.
   * <p>
   * This method needs to be called explicitly to support hot-plugging devices.
   * If a new device is connected or an existing device is disconnected, the list of input devices is updated accordingly.
   * </p>
   * <p>
   * IMPORTANT: This is a costly operation and should be called periodically to ensure that the list of input devices is up-to-date.
   * This method should not be called in the same interval as the polling of input devices.
   * </p>
   * <p>
   * This also triggers the {@link #onDeviceConnected(Consumer)} and {@link #onDeviceDisconnected(Consumer)} events when necessary.
   * </p>
   */
  protected void refreshDevices() {
    if (this.lastDeviceUpdate == 0 || System.currentTimeMillis() - this.lastDeviceUpdate < this.hotPlugInterval) {
      return;
    }

    this.lastDeviceUpdate = System.currentTimeMillis();
    final var oldDeviceIds = this.getAll().stream().map(InputDevice::getID).toList();
    var refreshedDevices = this.refreshInputDevices();
    var refreshedDeviceIds = refreshedDevices.stream().map(InputDevice::getID).toList();

    var devicesChanged = false;
    // Check for disconnected devices
    for (var currentDeviceId : oldDeviceIds) {
      if (!refreshedDeviceIds.contains(currentDeviceId)) {
        // Device was disconnected
        var disconnectedDevice = this.devices.stream().filter(d -> d.getID().equals(currentDeviceId)).findFirst().orElse(null);
        if (disconnectedDevice != null) {
          this.deviceDisconnectedListeners.forEach(listener -> listener.accept(disconnectedDevice));
          devicesChanged = true;
        }
      }
    }

    // Check for newly connected devices
    for (var connectedDeviceId : refreshedDeviceIds) {
      if (!oldDeviceIds.contains(connectedDeviceId)) {
        // New device connected
        InputDevice connectedDevice = refreshedDevices.stream().filter(d -> d.getID().equals(connectedDeviceId)).findFirst().orElse(null);
        if (connectedDevice != null) {
          this.deviceConnectedListeners.forEach(listener -> listener.accept(connectedDevice));
          devicesChanged = true;
        }
      }
    }

    if (devicesChanged) {
      this.devicesChangedListeners.forEach(Runnable::run);
    }

    this.setDevices(refreshedDevices);
  }

  /**
   * Refreshes the list of input devices by querying the underlying native API.
   *
   * @return A collection of currently available input devices.
   */
  protected abstract Collection<InputDevice> refreshInputDevices();

  /**
   * Applies a radial deadzone to two already normalized stick axes.
   *
   * @param values the values containing the two axes
   * @param xIndex the index of the horizontal axis
   * @param yIndex the index of the vertical axis
   * @param deadzone the radial deadzone in the range {@code 0..1}
   */
  protected static void applyCircularDeadzone(float[] values, int xIndex, int yIndex, float deadzone) {
    deadzone = Math.clamp(deadzone, 0f, 1f);
    var x = values[xIndex];
    var y = values[yIndex];
    var magnitude = (float) Math.hypot(x, y);
    if (magnitude <= deadzone || magnitude == 0f || deadzone >= 1f) {
      values[xIndex] = 0f;
      values[yIndex] = 0f;
      return;
    }

    var normalizedMagnitude = (Math.min(magnitude, 1f) - deadzone) / (1f - deadzone);
    values[xIndex] = x / magnitude * normalizedMagnitude;
    values[yIndex] = y / magnitude * normalizedMagnitude;
  }

  /**
   * Applies a one-dimensional deadzone and rescales the remaining range.
   *
   * @param value the normalized axis value
   * @param deadzone the deadzone in the range {@code 0..1}
   * @return the deadzone-adjusted value
   */
  protected static float applyDeadzone(float value, float deadzone) {
    deadzone = Math.clamp(deadzone, 0f, 1f);
    var magnitude = Math.abs(value);
    if (magnitude <= deadzone || deadzone >= 1f) {
      return 0f;
    }
    return Math.copySign((Math.min(magnitude, 1f) - deadzone) / (1f - deadzone), value);
  }
  
  public void onDevicesChanged(Runnable listener) {
    this.devicesChangedListeners.add(listener);
  }

  public void onDeviceConnected(Consumer<InputDevice> listener) {
    this.deviceConnectedListeners.add(listener);
  }

  public void onDeviceDisconnected(Consumer<InputDevice> listener) {
    this.deviceDisconnectedListeners.add(listener);
  }
}
