package de.gurkenlabs.litiengine.input;

public record DeviceComponent(ComponentType type, String name) {

  public boolean isAxis() {
    return type.isAxis();
  }
}
