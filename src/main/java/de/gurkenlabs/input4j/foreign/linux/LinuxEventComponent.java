package de.gurkenlabs.input4j.foreign.linux;


import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;

import java.lang.foreign.Arena;

final class LinuxEventComponent {
  final LinuxEventDevice device;
  final LinuxComponentType linuxComponentType;
  final ComponentType componentType;
  private final boolean axis;
  final boolean relative;
  final int min;
  final int max;
  final int flat;
  final int nativeCode;

  InputComponent inputComponent;

  LinuxEventComponent(Arena memoryArena, LinuxEventDevice device, int nativeType, int nativeCode) {
    this.device = device;
    this.relative = nativeType == LinuxEventDevice.EV_REL;
    this.axis = nativeType == LinuxEventDevice.EV_ABS;
    this.nativeCode = nativeCode;

    if (nativeType == LinuxEventDevice.EV_KEY) {
      this.linuxComponentType = LinuxComponentType.fromCode(nativeCode, false, false);
    } else if (this.axis || this.relative) {
      this.linuxComponentType = LinuxComponentType.fromCode(nativeCode, true, this.relative);
    } else {
      this.linuxComponentType = LinuxComponentType.UNKNOWN;
    }

    if (nativeType == LinuxEventDevice.EV_ABS) {
      input_absinfo absInfo = Linux.getAbsInfo(memoryArena, device.fd, nativeCode);
      if (absInfo == null) {
        this.min = Integer.MIN_VALUE;
        this.max = Integer.MAX_VALUE;
        this.flat = 0;
      } else {
        this.min = absInfo.minimum;
        this.max = absInfo.maximum;
        this.flat = absInfo.flat;
      }
    } else {
      this.min = Integer.MIN_VALUE;
      this.max = Integer.MAX_VALUE;
      this.flat = 0;
    }

    this.componentType = linuxComponentType.getComponentType(nativeCode, this.axis, this.relative);
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
      case BTN_SOUTH -> new InputComponent.ID(InputComponent.XInput.A, this.nativeCode);
      case BTN_EAST -> new InputComponent.ID(InputComponent.XInput.B, this.nativeCode);
      case BTN_NORTH -> new InputComponent.ID(InputComponent.XInput.X, this.nativeCode);
      case BTN_WEST -> new InputComponent.ID(InputComponent.XInput.Y, this.nativeCode);
      case BTN_TL -> new InputComponent.ID(InputComponent.XInput.LEFT_SHOULDER, this.nativeCode);
      case BTN_TR -> new InputComponent.ID(InputComponent.XInput.RIGHT_SHOULDER, this.nativeCode);
      case BTN_SELECT -> new InputComponent.ID(InputComponent.XInput.BACK, this.nativeCode);
      case BTN_START -> new InputComponent.ID(InputComponent.XInput.START, this.nativeCode);
      case BTN_MODE -> new InputComponent.ID(InputComponent.Button.get(10), this.nativeCode);
      case BTN_THUMBL -> new InputComponent.ID(InputComponent.XInput.LEFT_THUMB, this.nativeCode);
      case BTN_THUMBR -> new InputComponent.ID(InputComponent.XInput.RIGHT_THUMB, this.nativeCode);
      case BTN_TRIGGER_HAPPY1 -> new InputComponent.ID(InputComponent.XInput.DPAD_LEFT, this.nativeCode);
      case BTN_TRIGGER_HAPPY2 -> new InputComponent.ID(InputComponent.XInput.DPAD_RIGHT, this.nativeCode);
      case BTN_TRIGGER_HAPPY3 -> new InputComponent.ID(InputComponent.XInput.DPAD_UP, this.nativeCode);
      case BTN_TRIGGER_HAPPY4 -> new InputComponent.ID(InputComponent.XInput.DPAD_DOWN, this.nativeCode);
      case ABS_X -> new InputComponent.ID(InputComponent.XInput.LEFT_THUMB_X, this.nativeCode);
      case ABS_Y -> new InputComponent.ID(InputComponent.XInput.LEFT_THUMB_Y, this.nativeCode);
      case ABS_Z -> new InputComponent.ID(InputComponent.XInput.LEFT_TRIGGER, this.nativeCode);
      case ABS_RX -> new InputComponent.ID(InputComponent.XInput.RIGHT_THUMB_X, this.nativeCode);
      case ABS_RY -> new InputComponent.ID(InputComponent.XInput.RIGHT_THUMB_Y, this.nativeCode);
      case ABS_RZ -> new InputComponent.ID(InputComponent.XInput.RIGHT_TRIGGER, this.nativeCode);
      case ABS_HAT0X -> new InputComponent.Axis(InputComponent.ID.getNextId(), "DPAD_LEFT_RIGHT", this.nativeCode);
      case ABS_HAT0Y -> new InputComponent.Axis(InputComponent.ID.getNextId(), "DPAD_UP_DOWN", this.nativeCode);
      default -> {
        var name = this.linuxComponentType.name();
        yield switch (this.componentType) {
          case Axis -> new InputComponent.Axis(InputComponent.ID.getNextId(), name, this.nativeCode);
          case Button -> new InputComponent.Button(InputComponent.ID.getNextId(), name, this.nativeCode);
          case Key -> new InputComponent.ID(ComponentType.Key, InputComponent.ID.getNextId(), name, this.nativeCode);
          default -> new InputComponent.ID(ComponentType.Unknown, InputComponent.ID.getNextId(), name, this.nativeCode);
        };
      }
    };
  }
}
