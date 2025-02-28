package de.gurkenlabs.input4j;

import de.gurkenlabs.input4j.components.Axis;
import de.gurkenlabs.input4j.components.Button;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code InputComponent} class represents a component of an input device, such as a button or axis.
 * It holds information about the component's type, name, and whether it provides relative input.
 * The input data for the component is set by the associated {@link InputDevice} when polling.
 */
public final class InputComponent {
  private final InputDevice device;
  private final ID id;

  private final String originalName;
  private final boolean relative;

  private float data;

  public InputComponent(InputDevice device, ID id) {
    this(device, id, false);
  }

  /**
   * Creates a new instance of the InputComponent class.
   *
   * @param device   the input device associated with this component
   * @param relative true if the component provides relative input, false otherwise
   */
  public InputComponent(InputDevice device, ID id, boolean relative) {
    this(device, id, null, relative);
  }


  public InputComponent(InputDevice device, ID id, String originalName) {
    this(device, id, originalName, false);
  }

  /**
   * Creates a new instance of the InputComponent class.
   *
   * @param device       the input device associated with this component
   * @param originalName the name of the component
   * @param relative     true if the component provides relative input, false otherwise
   */
  public InputComponent(InputDevice device, ID id, String originalName, boolean relative) {
    this.device = device;
    this.id = id;
    this.originalName = originalName;
    this.relative = relative;
  }

  /**
   * Gets the input data for a specific input component.
   * The data is set by the {@link InputDevice} when polling.
   *
   * @return the input data for the specified component
   */
  public float getData() {
    return this.data;
  }

  /**
   * Gets the type of this component.
   *
   * @return the type of this component
   */
  public ComponentType getType() {
    return this.getId().type;
  }

  public ID getId() {
    return id;
  }

  /**
   * Gets the name of this component.
   *
   * @return the name of this component
   */
  public String getOriginalName() {
    return originalName;
  }

  /**
   * Checks if this component is a button.
   *
   * @return true if this component is a button, false otherwise
   */
  public boolean isButton() {
    return id.type == ComponentType.BUTTON;
  }

  /**
   * Checks if this component is an axis.
   *
   * @return true if this component is an axis, false otherwise
   */
  public boolean isAxis() {
    return id.type.isAxis();
  }

  /**
   * Checks if this component provides relative input.
   *
   * @return true if this component provides relative input, false otherwise
   */
  public boolean isRelative() {
    return relative;
  }

  /**
   * Gets the input device associated with this component.
   *
   * @return the input device associated with this component
   */
  public InputDevice getDevice() {
    return device;
  }

  /**
   * Sets the input data for this component.
   *
   * @param data the input data to set
   */
  public void setData(float data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return this.getId().name + ": " + this.getData();
  }

  /**
   * The {@code ID} class represents a unique identifier for input components.
   * It is used to manage and reference different input components, such as buttons, axes, and other controls.
   * The class ensures that each {@code ID} is unique and provides functionality to remap IDs for better accessibility.
   *
   * <p>Note: The {@code int} value needs to be unique. The {@link #getNextId(ComponentType, int)} method can be used to ensure this.</p>
   *
   * <p>Example usage:</p>
   * <pre>{@code
   * // Creating a new ID
   * InputComponent.ID customID = new InputComponent.ID(100, "CUSTOM_BUTTON");
   *
   * // Remapping an existing ID
   * InputComponent.ID remappedID = new InputComponent.ID(InputComponent.Button.get(1), "REMAPPED_BUTTON");
   * }</pre>
   *
   * <p>Remapping IDs for Controllers (see the predefined examples for XInput and DualShock4):</p>
   *
   * <p>XInput:</p>
   * <pre>{@code
   * public static final XInput A = new XInput(InputComponent.Button.get(0), "A");
   * }</pre>
   *
   * <p>DualShock4:</p>
   * <pre>{@code
   * public static final DualShock4 SQUARE = new DualShock4(InputComponent.Button.get(0), "SQUARE");
   * }</pre>
   */
  public static class ID {
    public final int id;
    public final int nativeId;
    public final ComponentType type;
    public String name;

    private static final List<ID> ids = new CopyOnWriteArrayList<>();

    /**
     * If you want to make this ID accessible with a different field, you can use this constructor.
     *
     * @param otherId the ID to remap
     */
    public ID(ID otherId) {
      this(otherId.type, otherId.id, otherId.name, 0);
    }

    public ID(ID otherId, int nativeId) {
      this(otherId.type, otherId.id, otherId.name, nativeId);
    }

    public ID(ID otherId, String name) {
      this(otherId.type, otherId.id, name, 0);
    }

    /**
     * If you want to make this ID available with a different name, you can use this constructor.
     *
     * @param otherId the ID to remap
     * @param name    the name of the new ID
     */
    public ID(ID otherId, String name, int nativeId) {
      this(otherId.type, otherId.id, name, nativeId);
    }

    public ID(ComponentType type, int id, String name) {
      this(type, id, name, 0);
    }

    public ID(ComponentType type, int id, String name, int nativeId) {
      this.type = type;
      this.id = id;
      this.name = name;
      this.nativeId = nativeId;
      // exclude remapped IDs, only add the original ID
      if (ids.stream().noneMatch(i -> i.equals(this))) {
        ids.add(this);
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ID identifier) {
        return identifier.type == this.type && identifier.id == this.id;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return 31 * type.hashCode() + id;
    }

    @Override
    public String toString() {
      return this.name;
    }

    public static int getNextAxisId() {
      return getNextId(ComponentType.AXIS, Axis.MAX_DEFAULT_AXIS_ID);
    }

    public static int getNextButtonId() {
      return getNextId(ComponentType.BUTTON, Button.MAX_DEFAULT_BUTTON_ID);
    }

    public static int getNextId(ComponentType type, int minId) {
      // reserve the id range for the default buttons and axes
      return Math.max(ids.stream().filter(i -> i.type == type).mapToInt(i -> i.id).max().orElse(0), minId) + 1;
    }

    public static ID get(int id) {
      return ids.stream().filter(i -> i.id == id).findFirst().orElse(null);
    }

    public static ID getButton(int id) {
      return ids.stream().filter(i -> i.type.isButton() && i.id == id).findFirst().orElse(null);
    }

    public static ID getAxis(int id) {
      return ids.stream().filter(i -> i.type.isAxis() && i.id == id).findFirst().orElse(null);
    }

    public static ID get(String name) {
      return ids.stream().filter(i -> i.name.equals(name)).findFirst().orElse(null);
    }
  }

  /**
   * Represents an event that is triggered when the value of an `InputComponent` changes.
   *
   * @param component The `InputComponent` whose value has changed
   * @param oldValue  The previous value of the `InputComponent`
   * @param newValue  The new value of the `InputComponent`
   */
  public record InputValueChangedEvent(InputComponent component, float oldValue, float newValue) {
  }
}
