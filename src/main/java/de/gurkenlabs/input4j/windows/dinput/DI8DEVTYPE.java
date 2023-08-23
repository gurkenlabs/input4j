package de.gurkenlabs.input4j.windows.dinput;

import java.util.Arrays;

enum DI8DEVTYPE {
  DI8DEVTYPE_DEVICE(0x11),
  DI8DEVTYPE_MOUSE(0x12),
  DI8DEVTYPE_KEYBOARD(0x13),
  DI8DEVTYPE_JOYSTICK(0x14),
  DI8DEVTYPE_GAMEPAD(0x15),
  DI8DEVTYPE_DRIVING(0x16),
  DI8DEVTYPE_FLIGHT(0x17),
  DI8DEVTYPE_1STPERSON(0x18),
  DI8DEVTYPE_DEVICECTRL(0x19),
  DI8DEVTYPE_SCREENPOINTER(0x1A),
  DI8DEVTYPE_REMOTE(0x1B),
  DI8DEVTYPE_SUPPLEMENTAL(0x1C);
  private final int devType;

  DI8DEVTYPE(int devType) {
    this.devType = devType;
  }

  public static DI8DEVTYPE fromDwDevType(int dwDevType) {
    //  The least-significant byte of the device type description code specifies the device type.
    var devType = (dwDevType & 0xFF);
    return Arrays.stream(DI8DEVTYPE.values()).filter(x -> x.devType == devType).findFirst().orElse(null);
  }

  public int getDevType() {
    return devType;
  }
}
