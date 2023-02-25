package de.gurkenlabs.litiengine.input;

public final class DeviceComponent {
  private final ComponentType type;
  private final String name;

  private float value;

  public DeviceComponent(ComponentType type, String name) {
    this.type = type;
    this.name = name;
  }

  public boolean isAxis() {
    return type.isAxis();
  }

  public float getValue() {
    return value;
  }

  public void setValue(float value) {
    this.value = value;
  }

  public ComponentType getType() {
    return type;
  }

  public String getName() {
    return name;
  }
}
