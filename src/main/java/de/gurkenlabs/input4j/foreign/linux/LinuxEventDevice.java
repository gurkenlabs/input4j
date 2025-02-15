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
  final int epfd;
  final List<LinuxEventComponent> componentList = new ArrayList<>();

  InputDevice inputDevice;


  public int version;

  public LinuxEventDevice(Arena memoryArena, String filename) {
    this.filename = filename;

    this.fd = Linux.open(memoryArena, this.filename);
    if (this.fd == Linux.ERROR) {
      this.name = null;
      this.id = null;
      this.version = 0;
      this.epfd = 0;
    } else {
      this.name = Linux.getEventDeviceName(memoryArena, this.fd);
      this.id = Linux.getEventDeviceId(memoryArena, this.fd);
      this.version = Linux.getEventDeviceVersion(memoryArena, this.fd);

      var epfd = Linux.epollCreate(memoryArena);
      if (Linux.epollCtl(memoryArena, epfd, this.fd) == Linux.ERROR) {
        log.log(Level.SEVERE, "Failed to add device to epoll");
        this.epfd = Linux.ERROR;
      } else {
        this.epfd = epfd;
      }
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

    Linux.close(memoryArena, this.fd);
  }
}
