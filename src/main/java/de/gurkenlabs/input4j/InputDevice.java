package de.gurkenlabs.input4j;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
  private final List<InputComponent> components = new CopyOnWriteArrayList<>();
  private final Collection<InputDeviceListener> listeners = ConcurrentHashMap.newKeySet();

  private final Function<InputDevice, float[]> pollCallback;
  private final BiConsumer<InputDevice, float[]> rumbleCallback;
  private float accuracyFactor;
  private boolean hasInputData;

  /**
   * Creates a new instance of the InputDevice class.
   *
   * @param instanceName   the name of the instance of the input device
   * @param productName    the name of the product of the input device
   * @param pollCallback   the function to be called when polling for input data from the device
   * @param rumbleCallback the function to be called when setting rumble intensity
   */
  public InputDevice(String instanceName, String productName, Function<InputDevice, float[]> pollCallback, BiConsumer<InputDevice, float[]> rumbleCallback) {
    this.instanceName = instanceName;
    this.productName = productName;
    this.pollCallback = pollCallback;
    this.rumbleCallback = rumbleCallback;
    this.setAccuracy(InputDevices.configure().getAccuracy());
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
  public List<InputComponent> getComponents() {
    return Collections.unmodifiableList(components);
  }

  /**
   * Gets an input component by its name.
   *
   * @param name the name of the input component
   * @return an Optional containing the input component if found, otherwise an empty Optional
   */
  public Optional<InputComponent> getComponent(String name) {
    if (name == null) {
      return Optional.empty();
    }

    return components.stream().filter(c -> c.getId().name.equals(name)).findFirst();
  }

  /**
   * Gets an input component by its ID.
   *
   * @param id the ID of the input component
   * @return an Optional containing the input component if found, otherwise an empty Optional
   */
  public Optional<InputComponent> getComponent(InputComponent.ID id) {
    if (id == null) {
      return Optional.empty();
    }

    return components.stream().filter(c -> c.getId().equals(id)).findFirst();
  }

  /**
   * Adds a collection of input components to the input device.
   *
   * @param components the input components to add
   */
  public void setComponents(Collection<InputComponent> components) {
    this.components.clear();
    this.components.addAll(components);
  }

  /**
   * Adds an input component to the input device.
   * If a component with the same ID already exists, it is replaced.
   *
   * @param component the input component to add
   */
  public void addComponent(InputComponent component) {
    Optional<InputComponent> existingComponent = components.stream()
            .filter(c -> c.equals(component))
            .findFirst();

    existingComponent.ifPresent(components::remove);
    components.add(component);
  }

  /**
   * Polls the input device for input data and updates the associated input components.
   */
  public void poll() {
    var polledData = this.pollCallback.apply(this);
    var hasInputData = false;

    var componentList = this.components;
    for (var i = 0; i < polledData.length && i < componentList.size(); i++) {
      var component = componentList.get(i);
      var oldData = component.getData();
      var newData = polledData[i];

      newData = Math.round(newData * this.accuracyFactor) / this.accuracyFactor;
      hasInputData |= newData != 0;
      if (oldData != newData) {
        hasInputData = true;
        component.setData(newData);

        var inputEvent = new InputEvent(component, oldData, newData);
        for (var listener : listeners) {
          listener.onValueChanged(inputEvent);
        }
      }
    }

    this.hasInputData = hasInputData;
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

  public void setAccuracy(int decimalPlaces) {
    if (decimalPlaces < 0) {
      throw new IllegalArgumentException("Decimal places must be a non-negative integer.");
    }

    this.accuracyFactor = (float) Math.pow(10, Math.min(decimalPlaces, 7));
  }

  /**
   * Checks if the input device has any input data.
   *
   * @return true if the input device has input data, false otherwise
   */
  public boolean hasInputData() {
    return this.hasInputData;
  }
}
