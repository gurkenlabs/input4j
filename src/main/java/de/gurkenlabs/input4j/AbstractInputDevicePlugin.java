package de.gurkenlabs.input4j;

import java.util.Collection;
import java.util.logging.Logger;

public abstract class AbstractInputDevicePlugin implements InputDevicePlugin {
  protected static final Logger log = Logger.getLogger(AbstractInputDevicePlugin.class.getPackage().getName());

  private Collection<InputDevice> devices;

  protected AbstractInputDevicePlugin() {
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
   * Sets the devices that are managed by this plugin.
   * <p>
   * <b>IMPORTANT</b>: This method needs to be called by the implementing class to set the devices that are managed by this plugin.
   *
   * @param devices The devices to set.
   */
  protected void setDevices(Collection<InputDevice> devices) {
    this.devices = devices;
  }
}
