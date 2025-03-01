package de.gurkenlabs.input4j;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents an input device.
 * An input device is a physical device that can provide input to a computer system.
 * It is composed of a collection of input components, such as buttons, axes, and other controls.
 * <p>
 * The input device can be polled for input data, which is then used to update the associated input components.
 * The input device can also be used to set rumble (vibration) intensity.
 * <br>
 * The input device can have multiple listeners that are notified when the value of an input component changes.
 * The listeners should implement the {@link InputDeviceListener} interface.
 * <br>
 * The input device should be closed to release any resources it holds.
 * </p>
 *
 * @see InputComponent
 * @see InputDeviceListener
 * @see de.gurkenlabs.input4j.InputComponent.InputValueChangedEvent
 * @see Closeable
 */
public final class InputDevice implements Closeable {
  private final String identifier;
  private final String name;
  private final String productName;
  private final List<InputComponent> components = new CopyOnWriteArrayList<>();
  private final Collection<InputDeviceListener> listeners = ConcurrentHashMap.newKeySet();
  private final Map<InputComponent.ID, Collection<Runnable>> buttonPressedListeners = new ConcurrentHashMap<>();
  private final Map<InputComponent.ID, Collection<Runnable>> buttonReleasedListeners = new ConcurrentHashMap<>();
  private final Map<InputComponent.ID, Collection<Consumer<Float>>> axisChangedListeners = new ConcurrentHashMap<>();

  private final Function<InputDevice, float[]> pollCallback;
  private final BiConsumer<InputDevice, float[]> rumbleCallback;
  private float accuracyFactor;
  private boolean hasInputData;

  /**
   * Creates a new instance of the InputDevice class.
   *
   * @param identifier     the identifier of the input device
   * @param name           the name of the instance of the input device
   * @param productName    the name of the product of the input device
   * @param pollCallback   the function to be called when polling for input data from the device
   * @param rumbleCallback the function to be called when setting rumble intensity
   */
  public InputDevice(String identifier, String name, String productName, Function<InputDevice, float[]> pollCallback, BiConsumer<InputDevice, float[]> rumbleCallback) {
    this.identifier = identifier;
    this.name = name;
    this.productName = productName;
    this.pollCallback = pollCallback;
    this.rumbleCallback = rumbleCallback;
    this.setAccuracy(InputDevices.configure().getAccuracy());
  }

  /**
   * Gets the identifier of the input device.
   * <p>
   * Note: The identifier is a unique string that identifies the input device on the system.
   * It can be different of different platforms and libraries and can be used
   * to uniquely identify the input device within the used input library during runtime.
   *
   * @return the identifier
   */
  public String getID() {
    return identifier;
  }

  /**
   * Gets the name of the instance of the input device.
   *
   * @return the instance name
   */
  public String getName() {
    return name;
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
    return components;
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
   * Finds the index of the specified component in the input device.
   *
   * @param id the component ID
   * @return the index of the component, or -1 if not found
   */
  public int getComponentIndex(InputComponent.ID id) {
    for (int i = 0; i < this.components.size(); i++) {
      if (this.components.get(i).getId().equals(id)) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Adds a collection of input components to the input device.
   *
   * @param components the input components to add
   */
  public void setComponents(List<InputComponent> components) {
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

        var inputEvent = new InputComponent.InputValueChangedEvent(component, oldData, newData);
        for (var listener : listeners) {
          listener.onValueChanged(inputEvent);
        }

        if (component.isButton()) {
          var id = component.getId();
          if (newData == 1 && buttonPressedListeners.containsKey(id)) {
            for (var listener : buttonPressedListeners.get(id)) {
              listener.run();
            }
          } else if (newData == 0 && buttonReleasedListeners.containsKey(id)) {
            for (var listener : buttonReleasedListeners.get(id)) {
              listener.run();
            }
          }
        }

        if (component.isAxis() && axisChangedListeners.containsKey(component.getId())) {
          for (var listener : axisChangedListeners.get(component.getId())) {
            listener.accept(newData);
          }
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
  public void setAccuracy(int decimalPlaces) {
    if (decimalPlaces < 0) {
      throw new IllegalArgumentException("Decimal places must be a non-negative integer.");
    }

    this.accuracyFactor = (float) Math.pow(10, Math.min(decimalPlaces, 7));
  }

  @Override
  public void close() {
    this.listeners.clear();
    this.buttonPressedListeners.clear();
    this.buttonReleasedListeners.clear();
  }

  /**
   * Checks if the input device has any input data.
   *
   * @return true if the input device has input data, false otherwise
   */
  public boolean hasInputData() {
    return this.hasInputData;
  }

  @Override
  public String toString() {
    return this.getName();
  }

  public void onInputValueChanged(InputDeviceListener listener) {
    this.listeners.add(listener);
  }

  public boolean onButtonPressed(int buttonId, Runnable runnable) {
    return this.onButtonPressed(InputComponent.ID.getButton(buttonId), runnable);
  }

  public boolean onButtonPressed(InputComponent.ID buttonId, Runnable runnable) {
    if (runnable == null || this.getComponent(buttonId).isEmpty()) {
      return false;
    }

    if (!this.buttonPressedListeners.containsKey(buttonId)) {
      this.buttonPressedListeners.put(buttonId, new CopyOnWriteArrayList<>());
    }

    this.buttonPressedListeners.get(buttonId).add(runnable);
    return true;
  }

  public boolean onButtonReleased(int buttonId, Runnable runnable) {
    return this.onButtonReleased(InputComponent.ID.getButton(buttonId), runnable);
  }

  public boolean onButtonReleased(InputComponent.ID buttonId, Runnable runnable) {
    if (runnable == null || this.getComponent(buttonId).isEmpty()) {
      return false;
    }

    if (!this.buttonReleasedListeners.containsKey(buttonId)) {
      this.buttonReleasedListeners.put(buttonId, new CopyOnWriteArrayList<>());
    }

    this.buttonReleasedListeners.get(buttonId).add(runnable);
    return true;
  }

  public void clearButtonPresedListeners(int buttonId) {
    this.clearButtonPressedListeners(InputComponent.ID.getButton(buttonId));
  }

  public void clearButtonPressedListeners(InputComponent.ID buttonId) {
    this.buttonPressedListeners.remove(buttonId);
  }

  public void clearButtonReleasedListeners(int buttonId) {
    this.clearButtonPressedListeners(InputComponent.ID.getButton(buttonId));
  }

  public void clearButtonReleasedListeners(InputComponent.ID buttonId) {
    this.buttonReleasedListeners.remove(buttonId);
  }

  public void removeButtonPressedListener(Runnable runnable) {
    for (var entry : this.buttonPressedListeners.entrySet()) {
      entry.getValue().remove(runnable);
    }
  }

  public void removeButtonReleasedListener(Runnable runnable) {
    for (var entry : this.buttonReleasedListeners.entrySet()) {
      entry.getValue().remove(runnable);
    }
  }

  public boolean onAxisChanged(int axisId, Consumer<Float> runnable) {
    return this.onAxisChanged(InputComponent.ID.getAxis(axisId), runnable);
  }

  public boolean onAxisChanged(InputComponent.ID axis, Consumer<Float> runnable) {
    if (runnable == null || this.getComponent(axis).isEmpty()) {
      return false;
    }

    if (!this.axisChangedListeners.containsKey(axis)) {
      this.axisChangedListeners.put(axis, new CopyOnWriteArrayList<>());
    }

    this.axisChangedListeners.get(axis).add(runnable);
    return true;
  }

  public void clearAxisChangedListeners(int axisId) {
    this.clearAxisChangedListeners(InputComponent.ID.getAxis(axisId));
  }

  public void clearAxisChangedListeners(InputComponent.ID axisId) {
    this.axisChangedListeners.remove(axisId);
  }

  public void removeAxisChangedListener(Consumer<Float> runnable) {
    for (var entry : this.axisChangedListeners.entrySet()) {
      entry.getValue().remove(runnable);
    }
  }
}
