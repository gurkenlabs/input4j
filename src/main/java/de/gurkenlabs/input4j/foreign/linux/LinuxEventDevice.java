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
  static final int FF_MAX = 0x7f;

  private static final Logger log = Logger.getLogger(LinuxEventDevice.class.getName());
  final String filename;
  final int fd;
  final String name;
  final input_id id;
  final List<LinuxEventComponent> componentList = new ArrayList<>();
  final boolean supportsForceFeedback;
  final int maxEffects;
  boolean openedReadOnly = false;

  InputDevice inputDevice;
  float[] currentValues;
  int currentEffectId = -1;
  float currentStrongMagnitude = 0f;
  float currentWeakMagnitude = 0f;
  boolean gainSet = false;

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
      this.maxEffects = 0;
    } else {
      this.name = Linux.getEventDeviceName(memoryArena, this.fd);
      this.id = Linux.getEventDeviceId(memoryArena, this.fd);
      this.version = Linux.getEventDeviceVersion(memoryArena, this.fd);
      this.maxEffects = Linux.getNumEffects(memoryArena, this.fd);
      this.supportsForceFeedback = this.maxEffects > 0;
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
      this.maxEffects = 0;
    } else {
      this.name = Linux.getEventDeviceName(memoryArena, this.fd);
      this.id = Linux.getEventDeviceId(memoryArena, this.fd);
      this.version = Linux.getEventDeviceVersion(memoryArena, this.fd);
      this.maxEffects = Linux.getNumEffects(memoryArena, this.fd);
      this.supportsForceFeedback = this.maxEffects > 0;
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
      case EV_FF -> FF_MAX;
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
