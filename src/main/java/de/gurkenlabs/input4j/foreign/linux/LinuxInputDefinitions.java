package de.gurkenlabs.input4j.foreign.linux;

/**
 * native definitions from input-event-codes.h and input.h.
 */
class LinuxInputDefinitions {
  public static final int EV_VERSION = 0x010001;
  public static final int EV_SYN = 0x00;
  public static final int EV_KEY = 0x01;
  public static final int EV_REL = 0x02;
  public static final int EV_ABS = 0x03;
  public static final int EV_MSC = 0x04;
  public static final int EV_SW = 0x05;
  public static final int EV_LED = 0x11;
  public static final int EV_SND = 0x12;
  public static final int EV_REP = 0x14;
  public static final int EV_FF = 0x15;
  public static final int EV_PWR = 0x16;
  public static final int EV_FF_STATUS = 0x17;
  public static final int EV_MAX = 0x1f;
  public static final int EV_CNT = (EV_MAX + 1);

  public static final int BTN_MISC = 0x100;
  public static final int BTN_0 = 0x100;
  public static final int BTN_1 = 0x101;
  public static final int BTN_2 = 0x102;
  public static final int BTN_3 = 0x103;
  public static final int BTN_4 = 0x104;
  public static final int BTN_5 = 0x105;
  public static final int BTN_6 = 0x106;
  public static final int BTN_7 = 0x107;
  public static final int BTN_8 = 0x108;
  public static final int BTN_9 = 0x109;

  public static final int BTN_MOUSE = 0x110;
  public static final int BTN_LEFT = 0x110;
  public static final int BTN_RIGHT = 0x111;
  public static final int BTN_MIDDLE = 0x112;
  public static final int BTN_SIDE = 0x113;
  public static final int BTN_EXTRA = 0x114;
  public static final int BTN_FORWARD = 0x115;
  public static final int BTN_BACK = 0x116;
  public static final int BTN_TASK = 0x117;

  public static final int BTN_JOYSTICK = 0x120;
  public static final int BTN_TRIGGER = 0x120;
  public static final int BTN_THUMB = 0x121;
  public static final int BTN_THUMB2 = 0x122;
  public static final int BTN_TOP = 0x123;
  public static final int BTN_TOP2 = 0x124;
  public static final int BTN_PINKIE = 0x125;
  public static final int BTN_BASE = 0x126;
  public static final int BTN_BASE2 = 0x127;
  public static final int BTN_BASE3 = 0x128;
  public static final int BTN_BASE4 = 0x129;
  public static final int BTN_BASE5 = 0x12a;
  public static final int BTN_BASE6 = 0x12b;
  public static final int BTN_DEAD = 0x12f;

  public static final int BTN_GAMEPAD = 0x130;
  public static final int BTN_A = 0x130;
  public static final int BTN_B = 0x131;
  public static final int BTN_C = 0x132;
  public static final int BTN_X = 0x133;
  public static final int BTN_Y = 0x134;
  public static final int BTN_Z = 0x135;
  public static final int BTN_TL = 0x136;
  public static final int BTN_TR = 0x137;
  public static final int BTN_TL2 = 0x138;
  public static final int BTN_TR2 = 0x139;
  public static final int BTN_SELECT = 0x13a;
  public static final int BTN_START = 0x13b;
  public static final int BTN_MODE = 0x13c;
  public static final int BTN_THUMBL = 0x13d;
  public static final int BTN_THUMBR = 0x13e;

  public static final int BTN_DIGI = 0x140;
  public static final int BTN_TOOL_PEN = 0x140;
  public static final int BTN_TOOL_RUBBER = 0x141;
  public static final int BTN_TOOL_BRUSH = 0x142;
  public static final int BTN_TOOL_PENCIL = 0x143;
  public static final int BTN_TOOL_AIRBRUSH = 0x144;
  public static final int BTN_TOOL_FINGER = 0x145;
  public static final int BTN_TOOL_MOUSE = 0x146;
  public static final int BTN_TOOL_LENS = 0x147;
  public static final int BTN_TOOL_QUINTTAP = 0x148;
  public static final int BTN_TOUCH = 0x14a;
  public static final int BTN_STYLUS = 0x14b;
  public static final int BTN_STYLUS2 = 0x14c;
  public static final int BTN_TOOL_DOUBLETAP = 0x14d;
  public static final int BTN_TOOL_TRIPLETAP = 0x14e;
  public static final int BTN_TOOL_QUADTAP = 0x14f;

  public static final int BTN_WHEEL = 0x150;
  public static final int BTN_GEAR_DOWN = 0x150;
  public static final int BTN_GEAR_UP = 0x151;

  public static final int BTN_TRIGGER_HAPPY = 0x2c0;
  public static final int BTN_TRIGGER_HAPPY1 = 0x2c0;
  public static final int BTN_TRIGGER_HAPPY2 = 0x2c1;
  public static final int BTN_TRIGGER_HAPPY3 = 0x2c2;
  public static final int BTN_TRIGGER_HAPPY4 = 0x2c3;
  public static final int BTN_TRIGGER_HAPPY5 = 0x2c4;
  public static final int BTN_TRIGGER_HAPPY6 = 0x2c5;
  public static final int BTN_TRIGGER_HAPPY7 = 0x2c6;
  public static final int BTN_TRIGGER_HAPPY8 = 0x2c7;
  public static final int BTN_TRIGGER_HAPPY9 = 0x2c8;
  public static final int BTN_TRIGGER_HAPPY10 = 0x2c9;
  public static final int BTN_TRIGGER_HAPPY11 = 0x2ca;
  public static final int BTN_TRIGGER_HAPPY12 = 0x2cb;
  public static final int BTN_TRIGGER_HAPPY13 = 0x2cc;
  public static final int BTN_TRIGGER_HAPPY14 = 0x2cd;
  public static final int BTN_TRIGGER_HAPPY15 = 0x2ce;
  public static final int BTN_TRIGGER_HAPPY16 = 0x2cf;
  public static final int BTN_TRIGGER_HAPPY17 = 0x2d0;
  public static final int BTN_TRIGGER_HAPPY18 = 0x2d1;
  public static final int BTN_TRIGGER_HAPPY19 = 0x2d2;
  public static final int BTN_TRIGGER_HAPPY20 = 0x2d3;
  public static final int BTN_TRIGGER_HAPPY21 = 0x2d4;
  public static final int BTN_TRIGGER_HAPPY22 = 0x2d5;
  public static final int BTN_TRIGGER_HAPPY23 = 0x2d6;
  public static final int BTN_TRIGGER_HAPPY24 = 0x2d7;
  public static final int BTN_TRIGGER_HAPPY25 = 0x2d8;
  public static final int BTN_TRIGGER_HAPPY26 = 0x2d9;
  public static final int BTN_TRIGGER_HAPPY27 = 0x2da;
  public static final int BTN_TRIGGER_HAPPY28 = 0x2db;
  public static final int BTN_TRIGGER_HAPPY29 = 0x2dc;
  public static final int BTN_TRIGGER_HAPPY30 = 0x2dd;
  public static final int BTN_TRIGGER_HAPPY31 = 0x2de;
  public static final int BTN_TRIGGER_HAPPY32 = 0x2df;
  public static final int BTN_TRIGGER_HAPPY33 = 0x2e0;
  public static final int BTN_TRIGGER_HAPPY34 = 0x2e1;
  public static final int BTN_TRIGGER_HAPPY35 = 0x2e2;
  public static final int BTN_TRIGGER_HAPPY36 = 0x2e3;
  public static final int BTN_TRIGGER_HAPPY37 = 0x2e4;
  public static final int BTN_TRIGGER_HAPPY38 = 0x2e5;
  public static final int BTN_TRIGGER_HAPPY39 = 0x2e6;
  public static final int BTN_TRIGGER_HAPPY40 = 0x2e7;


  /*
   * Relative axes
   */
  public static final int REL_X = 0x00;
  public static final int REL_Y = 0x01;
  public static final int REL_Z = 0x02;
  public static final int REL_RX = 0x03;
  public static final int REL_RY = 0x04;
  public static final int REL_RZ = 0x05;
  public static final int REL_HWHEEL = 0x06;
  public static final int REL_DIAL = 0x07;
  public static final int REL_WHEEL = 0x08;
  public static final int REL_MISC = 0x09;

  /*
   * 0x0a is reserved and should not be used in input drivers.
   * It was used by HID as REL_MISC+1 and userspace needs to detect if
   * the next REL_* event is correct or is just REL_MISC + n.
   * We define here REL_RESERVED so userspace can rely on it and detect
   * the situation described above.
   */
  public static final int REL_RESERVED = 0x0a;
  public static final int REL_WHEEL_HI_RES = 0x0b;
  public static final int REL_HWHEEL_HI_RES = 0x0c;
  public static final int REL_MAX = 0x0f;
  public static final int REL_CNT = (REL_MAX + 1);

  /*
   * Absolute axes
   */
  public static final int ABS_X = 0x00;
  public static final int ABS_Y = 0x01;
  public static final int ABS_Z = 0x02;
  public static final int ABS_RX = 0x03;
  public static final int ABS_RY = 0x04;
  public static final int ABS_RZ = 0x05;
  public static final int ABS_THROTTLE = 0x06;
  public static final int ABS_RUDDER = 0x07;
  public static final int ABS_WHEEL = 0x08;
  public static final int ABS_GAS = 0x09;
  public static final int ABS_BRAKE = 0x0a;
  public static final int ABS_HAT0X = 0x10;
  public static final int ABS_HAT0Y = 0x11;
  public static final int ABS_HAT1X = 0x12;
  public static final int ABS_HAT1Y = 0x13;
  public static final int ABS_HAT2X = 0x14;
  public static final int ABS_HAT2Y = 0x15;
  public static final int ABS_HAT3X = 0x16;
  public static final int ABS_HAT3Y = 0x17;
  public static final int ABS_PRESSURE = 0x18;
  public static final int ABS_DISTANCE = 0x19;
  public static final int ABS_TILT_X = 0x1a;
  public static final int ABS_TILT_Y = 0x1b;
  public static final int ABS_TOOL_WIDTH = 0x1c;
  public static final int ABS_VOLUME = 0x20;
  public static final int ABS_PROFILE = 0x21;
  public static final int ABS_MISC = 0x28;

  /*
   * 0x2e is reserved and should not be used in input drivers.
   * It was used by HID as ABS_MISC+6 and userspace needs to detect if
   * the next ABS_* event is correct or is just ABS_MISC + n.
   * We define here ABS_RESERVED so userspace can rely on it and detect
   * the situation described above.
   */
  public static final int ABS_RESERVED = 0x2e;

  public static final int ABS_MT_SLOT = 0x2f;
  public static final int ABS_MT_TOUCH_MAJOR = 0x30;
  public static final int ABS_MT_TOUCH_MINOR = 0x31;
  public static final int ABS_MT_WIDTH_MAJOR = 0x32;
  public static final int ABS_MT_WIDTH_MINOR = 0x33;
  public static final int ABS_MT_ORIENTATION = 0x34;
  public static final int ABS_MT_POSITION_X = 0x35;
  public static final int ABS_MT_POSITION_Y = 0x36;
  public static final int ABS_MT_TOOL_TYPE = 0x37;
  public static final int ABS_MT_BLOB_ID = 0x38;
  public static final int ABS_MT_TRACKING_ID = 0x39;
  public static final int ABS_MT_PRESSURE = 0x3a;
  public static final int ABS_MT_DISTANCE = 0x3b;
  public static final int ABS_MT_FIRST = ABS_MT_TOUCH_MAJOR;
  public static final int ABS_MT_LAST = ABS_MT_DISTANCE;
  public static final int ABS_MAX = 0x3f;
  public static final int ABS_CNT = (ABS_MAX + 1);

  public static final int BUS_PCI = 0x01;
  public static final int BUS_ISAPNP = 0x02;
  public static final int BUS_USB = 0x03;
  public static final int BUS_HIL = 0x04;
  public static final int BUS_BLUETOOTH = 0x05;
  public static final int BUS_VIRTUAL = 0x06;

  public static final int BUS_ISA = 0x10;
  public static final int BUS_I8042 = 0x11;
  public static final int BUS_XTKBD = 0x12;
  public static final int BUS_RS232 = 0x13;
  public static final int BUS_GAMEPORT = 0x14;
  public static final int BUS_PARPORT = 0x15;
  public static final int BUS_AMIGA = 0x16;
  public static final int BUS_ADB = 0x17;
  public static final int BUS_I2C = 0x18;
  public static final int BUS_HOST = 0x19;
  public static final int BUS_GSC = 0x1A;
  public static final int BUS_ATARI = 0x1B;
  public static final int BUS_SPI = 0x1C;
  public static final int BUS_RMI = 0x1D;
  public static final int BUS_CEC = 0x1E;
  public static final int BUS_INTEL_ISHTP = 0x1F;
  public static final int BUS_AMD_SFH = 0x20;

  /*
   * Values describing the status of a force-feedback effect
   */
  public static final int FF_STATUS_STOPPED = 0x00;
  public static final int FF_STATUS_PLAYING = 0x01;
  public static final int FF_STATUS_MAX = 0x01;


  /*
   * Force feedback effect types
   */
  public static final int FF_RUMBLE = 0x50;
  public static final int FF_PERIODIC = 0x51;
  public static final int FF_CONSTANT = 0x52;
  public static final int FF_SPRING = 0x53;
  public static final int FF_FRICTION = 0x54;
  public static final int FF_DAMPER = 0x55;
  public static final int FF_INERTIA = 0x56;
  public static final int FF_RAMP = 0x57;
  public static final int FF_EFFECT_MIN = FF_RUMBLE;
  public static final int FF_EFFECT_MAX = FF_RAMP;

  /*
   * Force feedback periodic effect types
   */
  public static final int FF_SQUARE = 0x58;
  public static final int FF_TRIANGLE = 0x59;
  public static final int FF_SINE = 0x5a;
  public static final int FF_SAW_UP = 0x5b;
  public static final int FF_SAW_DOWN = 0x5c;
  public static final int FF_CUSTOM = 0x5d;
  public static final int FF_WAVEFORM_MIN = FF_SQUARE;
  public static final int FF_WAVEFORM_MAX = FF_CUSTOM;

  /*
   * Set ff device properties
   */
  public static final int FF_GAIN = 0x60;
  public static final int FF_AUTOCENTER = 0x61;

  /*
   * ff->playback(effect_id = FF_GAIN) is the first effect_id to
   * cause a collision with another ff method, in this case ff->set_gain().
   * Therefore the greatest safe value for effect_id is FF_GAIN - 1,
   * and thus the total number of effects should never exceed FF_GAIN.
   */
  public static final int FF_MAX = 0x7f;
  public static final int FF_CNT = (FF_MAX + 1);

  public static final int USAGE_MOUSE = 0x00;
  public static final int USAGE_JOYSTICK = 0x01;
  public static final int USAGE_GAMEPAD = 0x02;
  public static final int USAGE_KEYBOARD = 0x03;
  public static final int USAGE_MAX = 0x0f;
}
