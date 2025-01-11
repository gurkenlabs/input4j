package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.ComponentType;

import java.util.Arrays;

public enum LinuxComponentType {
  BTN_UNKNOWN(-1),
  BTN_0(0x100),
  BTN_1(0x101),
  BTN_2(0x102),
  BTN_3(0x103),
  BTN_4(0x104),
  BTN_5(0x105),
  BTN_6(0x106),
  BTN_7(0x107),
  BTN_8(0x108),
  BTN_9(0x109),
  BTN_LEFT(0x110),
  BTN_RIGHT(0x111),
  BTN_MIDDLE(0x112),
  BTN_SIDE(0x113),
  BTN_EXTRA(0x114),
  BTN_TRIGGER(0x120),
  BTN_THUMB(0x121),
  BTN_THUMB2(0x122),
  BTN_TOP(0x123),
  BTN_TOP2(0x124),
  BTN_PINKIE(0x125),
  BTN_BASE(0x126),
  BTN_BASE2(0x127),
  BTN_BASE3(0x128),
  BTN_BASE4(0x129),
  BTN_BASE5(0x12A),
  BTN_BASE6(0x12B),
  BTN_DEAD(0x12F),
  BTN_A(0x130),
  BTN_B(0x131),
  BTN_C(0x132),
  BTN_X(0x133),
  BTN_Y(0x134),
  BTN_Z(0x135),
  BTN_TL(0x136),
  BTN_TR(0x137),
  BTN_TL2(0x138),
  BTN_TR2(0x139),
  BTN_SELECT(0x13A),
  BTN_MODE(0x13C),
  BTN_THUMBL(0x13D),
  BTN_THUMBR(0x13E),
  ABS_X(0x00),
  ABS_Y(0x01),
  ABS_Z(0x02),
  ABS_RX(0x03),
  ABS_RY(0x04),
  ABS_RZ(0x05),
  ABS_THROTTLE(0x06),
  ABS_RUDDER(0x07),
  ABS_WHEEL(0x08),
  ABS_GAS(0x09),
  ABS_BRAKE(0x0A),
  ABS_HAT0X(0x10),
  ABS_HAT0Y(0x11),
  ABS_HAT1X(0x12),
  ABS_HAT1Y(0x13),
  ABS_HAT2X(0x14),
  ABS_HAT2Y(0x15),
  ABS_HAT3X(0x16),
  ABS_HAT3Y(0x17),
  REL_X(0x00),
  REL_Y(0x01),
  REL_Z(0x02),
  REL_WHEEL(0x08),
  REL_HWHEEL(0x06),
  REL_DIAL(0x07),
  REL_MISC(0x09);

  private final int code;

  LinuxComponentType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static LinuxComponentType fromCode(int nativeCode, boolean axis, boolean relative) {
    return Arrays.stream(LinuxComponentType.values())
            .filter(type -> type.code == nativeCode && (!axis || (relative ? type.name().startsWith("REL") : type.name().startsWith("ABS"))))
            .findFirst()
            .orElse(BTN_UNKNOWN);
  }

  public ComponentType getComponentType(int nativeCode, boolean axis, boolean relative) {
    var type = fromCode(nativeCode, axis, relative);
    if (type == BTN_UNKNOWN) {
      if (!axis && nativeCode > 0 && nativeCode < 0x100) {
        // range from 1 to 255 is used for keys but overlaps with relative and absolute axes
        return ComponentType.Key;
      }

      return ComponentType.Unknown;
    }

    if (type.name().startsWith("BTN")) {
      return ComponentType.Button;
    } else if (type.name().startsWith("ABS_HAT")) {
      return ComponentType.POV;
    }

    return switch (type) {
      case ABS_X -> ComponentType.XAxis;
      case ABS_Y -> ComponentType.YAxis;
      case ABS_Z -> ComponentType.ZAxis;
      case ABS_RX -> ComponentType.RxAxis;
      case ABS_RY -> ComponentType.RyAxis;
      case ABS_RZ -> ComponentType.RzAxis;
      default -> ComponentType.Unknown;
    };
  }

  public boolean isAxis() {
    return this.name().startsWith("ABS") || this.name().startsWith("REL");
  }

  public String getIdentifier() {
    // remove BTN_, ABS_, REL_ prefixes
    return this.name().substring(4);
  }
}