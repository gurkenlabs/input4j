package de.gurkenlabs.input4j;

public enum ComponentType {
  Axis,
  Button,
  Key,
  Unknown;

  public boolean isAxis() { return this == Axis; }

  public boolean isButton() {
    return this == Button;
  }
}
