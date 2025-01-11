package de.gurkenlabs.input4j;

/**
 * The {@code InputComponent} class represents a component of an input device, such as a button or axis.
 * It holds information about the component's type, name, and whether it provides relative input.
 * The input data for the component is set by the associated {@link InputDevice} when polling.
 */
public final class InputComponent {
  private final InputDevice device;
  private final ComponentType type;

  // TODO: This needs to be unique for the library and definitely not platform-specific.
  private final String name;
  private final boolean relative;

  private float data;

  /**
   * Creates a new instance of the InputComponent class.
   *
   * @param device   the input device associated with this component
   * @param type     the type of the component (e.g., button, axis)
   * @param name     the name of the component
   * @param relative true if the component provides relative input, false otherwise
   */
  public InputComponent(InputDevice device, ComponentType type, String name, boolean relative) {
    this.device = device;
    this.type = type;
    this.name = name;
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
    return type;
  }

  /**
   * Gets the name of this component.
   *
   * @return the name of this component
   */
  public String getName() {
    return name;
  }

  /**
   * Checks if this component is an axis.
   *
   * @return true if this component is an axis, false otherwise
   */
  public boolean isAxis() {
    return type.isAxis();
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
    return this.getName() + ": " + this.getData();
  }
}
