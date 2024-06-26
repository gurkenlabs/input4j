package de.gurkenlabs.input4j;

public final class InputComponent {
  private final InputDevice device;
  private final ComponentType type;
  private final String name;

  private final boolean relative;

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

  public float getData() {
    return device.getData(this);
  }

  public ComponentType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public boolean isAxis() {
    return type.isAxis();
  }

  public boolean isRelative() {
    return relative;
  }

  public InputDevice getDevice() {
    return device;
  }

  @Override
  public String toString() {
    return this.getName() + ": " + device.getData(this);
  }
}
