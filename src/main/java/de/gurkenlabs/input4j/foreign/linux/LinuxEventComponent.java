package de.gurkenlabs.input4j.foreign.linux;


import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.components.Button;
import de.gurkenlabs.input4j.components.XInput;

final class LinuxEventComponent {
  final static String ID_DPAD_LEFT_RIGHT = "DPAD_LEFT_RIGHT";
  final static String ID_DPAD_UP_DOWN = "DPAD_UP_DOWN";

  final LinuxComponentType linuxComponentType;
  final ComponentType componentType;
  final boolean axis;
  final boolean relative;
  final int min;
  final int max;
  final int flat;
  final int fuzz;
  final int nativeType;
  final int nativeCode;

  InputComponent inputComponent;

  LinuxEventComponent(LinuxComponentType linuxComponentType, boolean axis, boolean relative, int nativeType, int nativeCode) {
    this(linuxComponentType, axis, relative, nativeType, nativeCode, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
  }

  LinuxEventComponent(LinuxComponentType linuxComponentType, boolean axis, boolean relative, int nativeType, int nativeCode, int min, int max, int flat, int fuzz) {
    this.linuxComponentType = linuxComponentType;
    this.axis = axis;
    this.relative = relative;
    this.componentType = linuxComponentType.getComponentType(nativeCode, this.axis, this.relative);

    this.nativeType = nativeType;
    this.nativeCode = nativeCode;

    this.min = min;
    this.max = max;
    this.flat = flat;
    this.fuzz = fuzz;
  }

  LinuxEventComponent(int nativeType, int nativeCode) {
    this(LinuxComponentType.fromCode(nativeCode, nativeType == LinuxEventDevice.EV_ABS, nativeType == LinuxEventDevice.EV_REL),
            nativeType == LinuxEventDevice.EV_ABS,
            nativeType == LinuxEventDevice.EV_REL,
            nativeType,
            nativeCode);
  }

  LinuxEventComponent(int nativeType, int nativeCode, input_absinfo absInfo) {
    this(LinuxComponentType.fromCode(nativeCode, nativeType == LinuxEventDevice.EV_ABS, nativeType == LinuxEventDevice.EV_REL),
            nativeType == LinuxEventDevice.EV_ABS,
            nativeType == LinuxEventDevice.EV_REL,
            nativeType,
            nativeCode,
            absInfo.minimum,
            absInfo.maximum,
            absInfo.flat,
            absInfo.fuzz);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LinuxEventComponent component) {
      return component.nativeType == this.nativeType && component.nativeCode == this.nativeCode;
    }
    return false;
  }

  boolean isRelative() {
    return relative;
  }

  boolean isAnalog() {
    return linuxComponentType.isAxis() && !this.isRelative();
  }

  float convertValue(float value) {
    if (linuxComponentType.isAxis() && !relative) {
      if (min == max) {
        return 0;
      }
      if (value > max) {
        value = max;
      } else if (value < min) {
        value = min;
      }
      return 2 * (value - min) / (max - min) - 1;
    } else {
      return value;
    }
  }

  float getDeadZone() {
    return flat / (2f * (max - min));
  }

  public InputComponent.ID getIdentifier() {
    return switch (linuxComponentType) {
      case BTN_SOUTH -> new InputComponent.ID(XInput.A, this.nativeCode);
      case BTN_EAST -> new InputComponent.ID(XInput.B, this.nativeCode);
      case BTN_NORTH -> new InputComponent.ID(XInput.X, this.nativeCode);
      case BTN_WEST -> new InputComponent.ID(XInput.Y, this.nativeCode);
      case BTN_TL -> new InputComponent.ID(XInput.LEFT_SHOULDER, this.nativeCode);
      case BTN_TR -> new InputComponent.ID(XInput.RIGHT_SHOULDER, this.nativeCode);
      case BTN_SELECT -> new InputComponent.ID(XInput.BACK, this.nativeCode);
      case BTN_START -> new InputComponent.ID(XInput.START, this.nativeCode);
      case BTN_MODE -> new InputComponent.ID(Button.BUTTON_10, this.nativeCode);
      case BTN_THUMBL -> new InputComponent.ID(XInput.LEFT_THUMB, this.nativeCode);
      case BTN_THUMBR -> new InputComponent.ID(XInput.RIGHT_THUMB, this.nativeCode);
      case BTN_TRIGGER_HAPPY1 -> new InputComponent.ID(XInput.DPAD_LEFT, this.nativeCode);
      case BTN_TRIGGER_HAPPY2 -> new InputComponent.ID(XInput.DPAD_RIGHT, this.nativeCode);
      case BTN_TRIGGER_HAPPY3 -> new InputComponent.ID(XInput.DPAD_UP, this.nativeCode);
      case BTN_TRIGGER_HAPPY4 -> new InputComponent.ID(XInput.DPAD_DOWN, this.nativeCode);
      case ABS_X -> new InputComponent.ID(XInput.LEFT_THUMB_X, this.nativeCode);
      case ABS_Y -> new InputComponent.ID(XInput.LEFT_THUMB_Y, this.nativeCode);
      case ABS_Z -> new InputComponent.ID(XInput.LEFT_TRIGGER, this.nativeCode);
      case ABS_RX -> new InputComponent.ID(XInput.RIGHT_THUMB_X, this.nativeCode);
      case ABS_RY -> new InputComponent.ID(XInput.RIGHT_THUMB_Y, this.nativeCode);
      case ABS_RZ -> new InputComponent.ID(XInput.RIGHT_TRIGGER, this.nativeCode);
      case ABS_HAT0X -> new InputComponent.ID(ComponentType.AXIS, InputComponent.ID.getNextId(), ID_DPAD_LEFT_RIGHT, this.nativeCode);
      case ABS_HAT0Y -> new InputComponent.ID(ComponentType.AXIS, InputComponent.ID.getNextId(), ID_DPAD_UP_DOWN, this.nativeCode);
      default -> {
        var name = this.linuxComponentType.name();
        yield switch (this.componentType) {
          case AXIS -> new InputComponent.ID(ComponentType.AXIS, InputComponent.ID.getNextId(), name, this.nativeCode);
          case BUTTON -> new InputComponent.ID(ComponentType.BUTTON, InputComponent.ID.getNextId(), name, this.nativeCode);
          case KEY -> new InputComponent.ID(ComponentType.KEY, InputComponent.ID.getNextId(), name, this.nativeCode);
          default -> new InputComponent.ID(ComponentType.UNKNOWN, InputComponent.ID.getNextId(), name, this.nativeCode);
        };
      }
    };
  }
}
