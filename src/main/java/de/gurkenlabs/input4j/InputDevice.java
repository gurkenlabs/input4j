package de.gurkenlabs.input4j;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents an input device.
 * An input device is a physical device that can provide input to a computer system,
 * such as a keyboard, mouse, or game controller.
 */
public final class InputDevice implements Closeable {
  private final String instanceName;
  private final String productName;
  private final Map<String, InputComponent> components = new LinkedHashMap<>();
  private final Collection<InputDeviceListener> listeners = ConcurrentHashMap.newKeySet();

  private final Function<InputDevice, float[]> pollCallback;
  private final BiConsumer<InputDevice, float[]> rumbleCallback;

  /**
   * Creates a new instance of the InputDevice class.
   *
   * @param instanceName the name of the instance of the input device
   * @param productName  the name of the product of the input device
   * @param pollCallback the function to be called when polling for input data from the device
   */
  public InputDevice(String instanceName, String productName, Function<InputDevice, float[]> pollCallback, BiConsumer<InputDevice, float[]> rumbleCallback) {
    this.instanceName = instanceName;
    this.productName = productName;
    this.pollCallback = pollCallback;
    this.rumbleCallback = rumbleCallback;
  }

  /**
   * Gets the name of the instance of the input device.
   *
   * @return the instance name
   */
  public String getInstanceName() {
    return instanceName;
  }

  /**
   * Gets the name of the product of the input device.
   *
   * @return the product name
   */
  public String getProductName() {
    return productName;
  }

  /**
   * Gets the collection of input components associated with the input device.
   *
   * @return the collection of input components
   */
  public Collection<InputComponent> getComponents() {
    return components.values();
  }

  public Optional<InputComponent> getComponent(String name) {
    if (name == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(components.get(name));
  }

  /**
   * Adds a collection of input components to the input device.
   *
   * @param components the input components to add
   */
  public void addComponents(Collection<InputComponent> components) {
    for (var component : components) {
      this.components.put(component.getName(), component);
    }
  }

  public void addComponent(InputComponent component) {
    this.components.put(component.getName(), component);
  }

  /**
   * Polls the input device for input data and updates the associated input components.
   */
  public void poll() {
    var polledData = this.pollCallback.apply(this);

    var componentList = new ArrayList<>(components.values());
    for (var i = 0; i < polledData.length && i < componentList.size(); i++) {
      var component = componentList.get(i);
      var oldData = component.getData();
      var newData = polledData[i];

      newData = Math.round(newData * 100) / 100.0f;
      if (oldData != newData) {
        component.setData(newData);

        for (var listener : listeners) {
          listener.onValueChanged(component, oldData, newData);
        }
      }
    }
  }

  /**
   * Sets the rumble (vibration) intensity for the input device.
   * The intensity values should be between 0 and 1.
   *
   * @param intensity The intensity values for the rumble.
   *                  <ul>
   *                    <li>If values are provided, they are used for the corresponding motors in order.</li>
   *                    <li>If fewer values are provided than the number of motors, the last provided value is used for the remaining motors.</li>
   *                    <li>If no values are provided, the rumble is stopped (intensity set to 0).</li>
   *                  </ul>
   */
  public void rumble(float... intensity) {
    if (this.rumbleCallback == null) {
      // rumble not supported if no rumble callback is provided by the library
      return;
    }

    this.rumbleCallback.accept(this, intensity);
  }

  @Override
  public void close() {
    listeners.clear();
  }
}
