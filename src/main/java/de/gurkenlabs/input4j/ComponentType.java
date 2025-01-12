package de.gurkenlabs.input4j;

public enum ComponentType {
  Axis,
  Button,
  DPad,
  Key,
  Unknown;

  public boolean isAxis() { return this == Axis; }

  public boolean isButton() {
    return this == Button;
  }
}
