package de.gurkenlabs.input4j.foreign.linux;


import de.gurkenlabs.input4j.ComponentType;

import java.lang.foreign.Arena;

final class LinuxEventComponent {
  final LinuxEventDevice device;
  final LinuxComponentType linuxComponentType;
  final ComponentType componentType;
  final boolean relative;
  final int min;
  final int max;
  final int flat;

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
}
