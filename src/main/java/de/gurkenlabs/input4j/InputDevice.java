package de.gurkenlabs.input4j;

import java.util.*;
import java.util.function.Function;

/**
 * Represents an input device.
 * An input device is a physical device that can provide input to a computer system,
 * such as a keyboard, mouse, or game controller.
 */
public final class InputDevice {
  private final UUID instance;
  private final UUID product;
  private final String instanceName;
  private final String productName;
  private final Function<InputDevice, float[]> pollCallback;
  private final LinkedHashMap<InputComponent, Float> components = new LinkedHashMap<>();

  /**
   * Creates a new instance of the InputDevice class.
   *
   * @param instance     the unique identifier for the instance of the input device
   * @param product      the unique identifier for the product of the input device
   * @param instanceName the name of the instance of the input device
   * @param productName  the name of the product of the input device
   * @param pollCallback the function to be called when polling for input data from the device
   */
  public InputDevice(UUID instance, UUID product, String instanceName, String productName, Function<InputDevice, float[]> pollCallback) {
    this.instance = instance;
    this.product = product;
    this.instanceName = instanceName;
    this.productName = productName;
    this.pollCallback = pollCallback;
  }

  /**
   * Gets the unique identifier for the instance of the input device.
   *
   * @return the instance identifier
   */
  public UUID getInstance() {
    return instance;
  }

  /**
   * Gets the unique identifier for the product of the input device.
   *
   * @return the product identifier
   */
  public UUID getProduct() {
    return product;
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
    return components.keySet();
  }

  /**
   * Adds a collection of input components to the input device.
   *
   * @param components the input components to add
   */
  public void addComponents(Collection<InputComponent> components) {
    for (var component : components) {
      this.components.put(component, 0f);
    }
  }

  /**
   * Polls the input device for input data and updates the associated input components.
   */
  public void poll() {
    var polledData = this.pollCallback.apply(this);

    var componentList = new ArrayList<>(components.keySet());
    for (var i = 0; i < polledData.length; i++) {
      var component = componentList.get(i);
      this.components.put(component, polledData[i]);
    }
  }

  /**
   * Gets the input data for a specific input component.
   *
   * @param component the input component to get the data for
   * @return the input data for the specified component
   */
  public float getData(InputComponent component) {
    return this.components.get(component);
  }
}
