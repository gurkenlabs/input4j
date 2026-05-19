package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.InputDevice;

import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class LinuxEventDevice {
  static final int EV_VERSION = 0x010001;
  static final int EV_SYN = 0x00;
  static final int EV_KEY = 0x01;
  static final int EV_REL = 0x02;
  static final int EV_ABS = 0x03;
  static final int EV_MSC = 0x04;
  static final int EV_SW = 0x05;
  static final int EV_LED = 0x11;
  static final int EV_SND = 0x12;
  static final int EV_REP = 0x14;
  static final int EV_FF = 0x15;
  static final int EV_PWR = 0x16;
  static final int EV_FF_STATUS = 0x17;
  static final int EV_MAX = 0x1f;

  static final int KEY_MAX = 0x2ff;
  static final int REL_MAX = 0x0f;
  static final int ABS_MAX = 0x3f;

  private static final Logger log = Logger.getLogger(LinuxEventDevice.class.getName());
  final String filename;
  final int fd;
  final String name;
  final input_id id;
  final List<LinuxEventComponent> componentList = new ArrayList<>();

  /**
   * Whether the device supports force feedback (rumble).
   * Requires both write access to the device node and FF_RUMBLE capability
   * reported via EVIOCGBIT(EV_FF). The kernel auto-sets FF_RUMBLE when
   * FF_PERIODIC is available, so this covers both native and emulated rumble.
   */
  final boolean supportsForceFeedback;

  /**
   * Whether the device reports FF_RUMBLE (0x50) capability via EVIOCGBIT(EV_FF).
   * This is the authoritative way to detect rumble support — checking maxEffects
   * alone is insufficient because a device may have effect slots but not support
   * the rumble effect type.
   */
  final boolean supportsRumble;

  /**
   * Whether the device reports FF_SINE (0x5a) capability via EVIOCGBIT(EV_FF).
   * Used as a fallback when FF_RUMBLE is not available — the kernel can emulate
   * rumble via FF_PERIODIC/FF_SINE, but some older drivers only expose FF_SINE
   * without the kernel's auto-emulation of FF_RUMBLE.
   */
  final boolean supportsSine;

  /**
   * Whether the device reports FF_GAIN (0x60) capability via EVIOCGBIT(EV_FF).
   * The kernel silently ignores FF_GAIN writes when unsupported (returns bytes
   * written without applying gain), so this flag prevents false-positive gain
   * detection that leaves rumble at zero intensity on some controllers.
   */
  final boolean supportsGain;
  final int maxEffects;
  boolean openedReadOnly = false;

  InputDevice inputDevice;
  float[] currentValues;
  int currentEffectId = -1;
  float currentStrongMagnitude = 0f;
  float currentWeakMagnitude = 0f;

  public int version;

  public LinuxEventDevice(Arena memoryArena, String filename) {
    this.filename = filename;

    this.fd = Linux.open(memoryArena, this.filename);
    this.openedReadOnly = (this.fd != Linux.ERROR);
    if (this.fd == Linux.ERROR) {
      this.name = null;
      this.id = null;
      this.version = 0;
      this.supportsForceFeedback = false;
      this.supportsRumble = false;
      this.supportsSine = false;
      this.supportsGain = false;
      this.maxEffects = 0;
    } else {
      this.name = Linux.getEventDeviceName(memoryArena, this.fd);
      this.id = Linux.getEventDeviceId(memoryArena, this.fd);
      this.version = Linux.getEventDeviceVersion(memoryArena, this.fd);
      this.maxEffects = Linux.getNumEffects(memoryArena, this.fd);
      byte[] ffBits = Linux.getBits(memoryArena, EV_FF, this.fd);
      if (ffBits != null) {
        this.supportsRumble = isBitSet(ffBits, Linux.FF_RUMBLE);
        this.supportsSine = isBitSet(ffBits, Linux.FF_SINE);
        this.supportsGain = isBitSet(ffBits, Linux.FF_GAIN);
      } else {
        this.supportsRumble = false;
        this.supportsSine = false;
        this.supportsGain = false;
      }
      // Force feedback requires write access and rumble (or sine fallback) support
      this.supportsForceFeedback = !this.openedReadOnly && (this.supportsRumble || this.supportsSine);
    }
  }

  public LinuxEventDevice(Arena memoryArena, String filename, boolean forceRumble) {
    this.filename = filename;

    int openedFd = Linux.ERROR;
    boolean isReadOnly = false;

    if (forceRumble) {
      int[] lastErrno = new int[1];
      openedFd = Linux.openRdwr(memoryArena, this.filename, lastErrno);
      if (openedFd == Linux.ERROR && lastErrno[0] == Linux.EACCES) {
        log.log(Level.INFO, "No write access for ''{0}'', retrying read-only", this.filename);
        openedFd = Linux.open(memoryArena, this.filename);
        isReadOnly = (openedFd != Linux.ERROR);
      }
    } else {
      openedFd = Linux.open(memoryArena, this.filename);
      isReadOnly = (openedFd != Linux.ERROR);
    }

    this.fd = openedFd;
    this.openedReadOnly = isReadOnly;

    if (this.fd == Linux.ERROR) {
      this.name = null;
      this.id = null;
      this.version = 0;
      this.supportsForceFeedback = false;
      this.supportsRumble = false;
      this.supportsSine = false;
      this.supportsGain = false;
      this.maxEffects = 0;
    } else {
      this.name = Linux.getEventDeviceName(memoryArena, this.fd);
      this.id = Linux.getEventDeviceId(memoryArena, this.fd);
      this.version = Linux.getEventDeviceVersion(memoryArena, this.fd);
      this.maxEffects = Linux.getNumEffects(memoryArena, this.fd);
      byte[] ffBits = Linux.getBits(memoryArena, EV_FF, this.fd);
      if (ffBits != null) {
        this.supportsRumble = isBitSet(ffBits, Linux.FF_RUMBLE);
        this.supportsSine = isBitSet(ffBits, Linux.FF_SINE);
        this.supportsGain = isBitSet(ffBits, Linux.FF_GAIN);
      } else {
        this.supportsRumble = false;
        this.supportsSine = false;
        this.supportsGain = false;
      }
      // Force feedback requires write access and rumble (or sine fallback) support
      this.supportsForceFeedback = !isReadOnly && (this.supportsRumble || this.supportsSine);
    }
  }

  public static boolean isBitSet(byte[] bits, int bit) {
    return (bits[bit / 8] & (1 << (bit % 8))) != 0;
  }

  public static int getMaxBits(int evtype) {
    return switch (evtype) {
      case EV_SYN -> EV_MAX;
      case EV_KEY -> KEY_MAX;
      case EV_REL -> REL_MAX;
      case EV_ABS -> ABS_MAX;
      case EV_FF -> Linux.FF_CNT;
      default -> 0;
    };
  }

  public void close(Arena memoryArena) {
    if (this.fd == Linux.ERROR) {
      return;
    }

    if (this.currentEffectId != -1) {
      var stopEvent = new input_event();
      stopEvent.type = (short) EV_FF;
      stopEvent.code = (short) this.currentEffectId;
      stopEvent.value = 0;
      Linux.writeEvent(memoryArena, this.fd, stopEvent);
      Linux.removeEffect(memoryArena, this.fd, this.currentEffectId);
      this.currentEffectId = -1;
    }

    Linux.close(memoryArena, this.fd);
  }

  public LinuxEventComponent getNativeComponent(input_event inputEvent) {
    for (var component : this.componentList) {
      if (component.nativeType == inputEvent.type && component.nativeCode == inputEvent.code) {
        return component;
      }
    }
    return null;
  }
}
