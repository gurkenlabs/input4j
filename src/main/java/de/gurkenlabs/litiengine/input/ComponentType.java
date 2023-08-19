package de.gurkenlabs.litiengine.input;

public enum ComponentType {
  XAxis,
  YAxis,
  ZAxis,
  RxAxis,
  RyAxis,
  RzAxis,
  Slider,
  Button,
  Key,
  POV,
  Unknown;

  public boolean isAxis() {
    return switch (this) {
      case XAxis, YAxis, ZAxis, RxAxis, RyAxis, RzAxis -> true;
      default -> false;
    };
  }

  public boolean isButton() {
    return this == Button;
  }
}
