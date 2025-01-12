package de.gurkenlabs.input4j.foreign.linux;


import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;

import java.lang.foreign.Arena;

final class LinuxEventComponent {
  final LinuxEventDevice device;
  final LinuxComponentType linuxComponentType;
  final ComponentType componentType;
  final boolean relative;
  final int min;
  final int max;
  final int flat;

  InputComponent inputComponent;

  LinuxEventComponent(Arena memoryArena, LinuxEventDevice device, int nativeType, int nativeCode) {
    this.device = device;
    this.relative = nativeType == LinuxEventDevice.EV_REL;

    if (nativeType == LinuxEventDevice.EV_KEY) {
      this.linuxComponentType = LinuxComponentType.fromCode(nativeCode, false, false);
    } else if (nativeType == LinuxEventDevice.EV_ABS || this.relative) {
      this.linuxComponentType = LinuxComponentType.fromCode(nativeCode, true, nativeType == LinuxEventDevice.EV_REL);
    } else {
      this.linuxComponentType = LinuxComponentType.UNKNOWN;
    }

    if (nativeType == LinuxEventDevice.EV_ABS) {
      input_absinfo absInfo = Linux.getAbsInfo(memoryArena, device.fd, nativeCode);
      if(absInfo == null) {
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

    this.componentType = linuxComponentType.getComponentType(nativeCode, linuxComponentType.isAxis(), this.relative);
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

  public InputComponent.ID getIdentifier(){
    switch(linuxComponentType){
      case BTN_SOUTH: return InputComponent.XInput.A;
      case BTN_EAST: return InputComponent.XInput.B;
      case BTN_NORTH: return InputComponent.XInput.X;
      case BTN_WEST: return InputComponent.XInput.Y;
      case BTN_TL: return InputComponent.XInput.LEFT_SHOULDER;
      case BTN_TR: return InputComponent.XInput.RIGHT_SHOULDER;
      case BTN_SELECT: return InputComponent.XInput.BACK;
      case BTN_START: return InputComponent.XInput.START;
      case BTN_MODE: return InputComponent.Button.get(10);
      case BTN_THUMBL: return InputComponent.XInput.LEFT_THUMB;
      case BTN_THUMBR: return InputComponent.XInput.RIGHT_THUMB;
      case BTN_TRIGGER_HAPPY1: return InputComponent.XInput.DPAD_LEFT;
      case BTN_TRIGGER_HAPPY2: return InputComponent.XInput.DPAD_RIGHT;
      case BTN_TRIGGER_HAPPY3: return InputComponent.XInput.DPAD_UP;
      case BTN_TRIGGER_HAPPY4: return InputComponent.XInput.DPAD_DOWN;
      case ABS_X: return InputComponent.XInput.LEFT_THUMB_X;
      case ABS_Y: return InputComponent.XInput.LEFT_THUMB_Y;
      case ABS_Z: return InputComponent.XInput.LEFT_TRIGGER;
      case ABS_RX: return InputComponent.XInput.RIGHT_THUMB_X;
      case ABS_RY: return InputComponent.XInput.RIGHT_THUMB_Y;
      case ABS_RZ: return InputComponent.XInput.RIGHT_TRIGGER;
      case ABS_HAT0X: return new InputComponent.Axis(InputComponent.ID.getNextId(), "DPAD_LEFT_RIGHT");
      case ABS_HAT0Y: return new InputComponent.Axis(InputComponent.ID.getNextId(), "DPAD_UP_DOWN");
      default:
        var name = this.linuxComponentType.name();
        return switch (this.componentType) {
          case Axis -> new InputComponent.Axis(InputComponent.ID.getNextId(), name);
          case Button -> new InputComponent.Button(InputComponent.ID.getNextId(), name);
          case Key -> new InputComponent.ID(ComponentType.Key, InputComponent.ID.getNextId(), name);
          default -> new InputComponent.ID(ComponentType.Unknown, InputComponent.ID.getNextId(), name);
        };
    }
  }
}
