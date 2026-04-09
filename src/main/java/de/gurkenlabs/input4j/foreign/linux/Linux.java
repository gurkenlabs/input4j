package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

class Linux {
  private Linux() {}

  private static final Logger log = Logger.getLogger(Linux.class.getName());
  final static int ERROR = -1;
  final static int EAGAIN = 11;

  final static int O_RDONLY = 0;
  final static int O_RDWR = 2;
  final static int O_NONBLOCK = 0x800;

  final static int _IOC_READ = 2;
  final static int NAME_BUFFER_SIZE = 1024;
  final static String ERRNO = "errno";
  final static String HANDLE_STRERROR = "strerror";
  final static String HANDLE_OPEN = "open";
  final static String HANDLE_CLOSE = "close";
  final static String HANDLE_IOCTL = "ioctl";
  final static String HANDLE_READ = "read";
  final static String HANDLE_SELECT = "select";
  final static String HANDLE_WRITE = "write";

  private static final boolean IS_32_BIT = is32BitSystem();

  private static boolean is32BitSystem() {
    String osArch = System.getProperty("os.arch", "").toLowerCase();
    return osArch.equals("arm") || osArch.equals("i386") || osArch.equals("i486")
        || osArch.equals("i586") || osArch.equals("i686") || osArch.equals("x86")
        || osArch.startsWith("armv");
  }

  private static int EVIOCGVERSION = _IOR('E', 0x01, JAVA_INT.byteSize());
  private final static int EVIOCGID = _IOR('E', 0x02, input_id.$LAYOUT.byteSize());
  private final static int EVIOCGNAME = _IOC(_IOC_READ, 'E', 0x06, NAME_BUFFER_SIZE);
  private static final int EVIOCGEFFECTS = _IOR('E', 0x84, JAVA_INT.byteSize());
  private static final int EVIOCSFF = _IOW('E', 0x80, ff_effect.$LAYOUT.byteSize());
  private static final int EVIOCRMFF = _IOW('E', 0x81, JAVA_INT.byteSize());

  static final int FF_RUMBLE = 0x50;
  static final int FF_PERIODIC = 0x51;
  static final int FF_CONSTANT = 0x52;
  static final int FF_SPRING = 0x53;
  static final int FF_FRICTION = 0x54;
  static final int FF_DAMPER = 0x55;
  static final int FF_INERTIA = 0x56;
  static final int FF_RAMP = 0x57;
  static final int FF_EFFECT_MIN = 0x50;
  static final int FF_EFFECT_MAX = 0x5f;
  static final int FF_CNT = FF_EFFECT_MAX + 1;

  static final int FF_STATUS_STOPPED = 0x00;
  static final int FF_STATUS_PLAYING = 0x01;
  static final int FF_STATUS_MAX = 0x01;

  static final int FF_GAIN = 0x60;

  private static int EVIOCGKEY(int len) {
    return _IOC(_IOC_READ, 'E', 0x18, len);
  }

  private static int EVIOCGBIT(int evtype, int len) {
    return _IOC(_IOC_READ, 'E', 0x20 + evtype, len);
  }

  private static int EVIOCGABS(int absAxis) {
    return _IOC(_IOC_READ, 'E', 0x40 + absAxis, (int) input_absinfo.$LAYOUT.byteSize());
  }

  private static final VarHandle errnoHandle;
  private static final MethodHandle strerror;
  private static final Map<String, MethodHandle> handles = new HashMap<>();

  static {
    StructLayout capturedStateLayout = Linker.Option.captureStateLayout();

    errnoHandle = capturedStateLayout.varHandle(MemoryLayout.PathElement.groupElement(ERRNO));
    strerror = downcallHandle(HANDLE_STRERROR, FunctionDescriptor.of(ADDRESS, JAVA_INT));

    ValueLayout sizeT = IS_32_BIT ? JAVA_INT : JAVA_LONG;

    handles.put(HANDLE_OPEN, downcallHandle(HANDLE_OPEN, FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT), ERRNO));
    handles.put(HANDLE_CLOSE, downcallHandle(HANDLE_CLOSE, FunctionDescriptor.of(JAVA_INT, JAVA_INT), ERRNO));
    handles.put(HANDLE_IOCTL, downcallHandle(HANDLE_IOCTL, FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS), ERRNO));
    handles.put(HANDLE_READ, downcallHandle(HANDLE_READ, FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, sizeT), ERRNO));
    handles.put(HANDLE_SELECT, downcallHandle(HANDLE_SELECT, FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, sizeT), ERRNO));
    handles.put(HANDLE_WRITE, downcallHandle(HANDLE_WRITE, FunctionDescriptor.of(sizeT, JAVA_INT, ADDRESS, sizeT), ERRNO));
  }

  /**
   * Open a file descriptor for the given file name.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fileName    the file name to open
   * @return the file descriptor or -1 if an error occurred
   */
  static int open(Arena memoryArena, String fileName) {
    var filenameMemorySegment = memoryArena.allocateFrom(fileName);
    return invoke(HANDLE_OPEN, memoryArena, filenameMemorySegment, O_RDONLY | O_NONBLOCK);
  }

  /**
   * Open a file descriptor for the given file name in read/write mode.
   *
   * <p>
   * This is required for force feedback support as writing to the device
   * is needed to play/stop effects.
   * </p>
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fileName    the file name to open
   * @return the file descriptor or -1 if an error occurred
   */
  static int openRdwr(Arena memoryArena, String fileName) {
    var filenameMemorySegment = memoryArena.allocateFrom(fileName);
    return invoke(HANDLE_OPEN, memoryArena, filenameMemorySegment, O_RDWR | O_NONBLOCK);
  }

  /**
   * Close the file descriptor.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor to close
   */
  static void close(Arena memoryArena, int fd) {
    invoke(HANDLE_CLOSE, memoryArena, fd);
  }

 /**
   * Read an input event from the device.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor of the event device
   * @return the input event or null if no more events are available
   */
  public static input_event read(Arena memoryArena, int fd) {
    MemorySegment inputEventMemorySegment = memoryArena.allocate(input_event.$LAYOUT);
    int result = invoke("read", memoryArena, fd, inputEventMemorySegment, input_event.$LAYOUT.byteSize());
    if (result == ERROR) {
      log.log(Level.FINE, "No more events to read from device ({0})", fd);
      return null;
    }

    return input_event.read(inputEventMemorySegment);
  }

  /**
   * Get the name of the event device.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor of the event device
   * @return the name of the event device or null if an error occurred
   */
  static String getEventDeviceName(Arena memoryArena, int fd) {
    var nameMemorySegment = memoryArena.allocateFrom(JAVA_CHAR, new char[NAME_BUFFER_SIZE]);
    var result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGNAME, nameMemorySegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get device name for device ({0})", fd);
      return null;
    }

    return nameMemorySegment.getString(0);
  }

  /**
   * Get the version of the event device.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor of the event device
   * @return the version of the event device or 0 if an error occurred
   */
  static int getEventDeviceVersion(Arena memoryArena, int fd) {
    var versionMemorySegment = memoryArena.allocate(JAVA_INT);
    var result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGVERSION, versionMemorySegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get device version for device ({0})", fd);
      return 0;
    }

    return versionMemorySegment.get(JAVA_INT, 0);
  }

  /**
   * Get the id of the event device.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor of the event device
   * @return the id of the event device or null if an error occurred
   */
  static input_id getEventDeviceId(Arena memoryArena, int fd) {
    var inputIdMemorySegment = memoryArena.allocate(input_id.$LAYOUT);
    var result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGID, inputIdMemorySegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get device id for device ({0})", fd);
      return null;
    }

    return input_id.read(inputIdMemorySegment);
  }

  static input_absinfo getAbsInfo(Arena memoryArena, int fd, int absAxis) {
    MemorySegment absInfoSegment = memoryArena.allocate(input_absinfo.$LAYOUT);
    int result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGABS(absAxis), absInfoSegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get abs info for axis ({0})", absAxis);
      return null;
    }
    return input_absinfo.read(absInfoSegment);
  }

  static int getNumEffects(Arena memoryArena, int fd) {
    MemorySegment numEffectsSegment = memoryArena.allocate(JAVA_INT);
    int result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGEFFECTS, numEffectsSegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get number of device effects ({0})", fd);
      return ERROR;
    }
    return numEffectsSegment.get(JAVA_INT, 0);
  }

  /**
   * Upload a force feedback effect to the device.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor of the event device
   * @param effect      the effect to upload
   * @return the effect ID assigned by the device, or -1 if an error occurred
   */
  static int uploadEffect(Arena memoryArena, int fd, ff_effect effect) {
    var effectSegment = memoryArena.allocate(ff_effect.$LAYOUT);
    effect.write(effectSegment);
    int result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCSFF, effectSegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to upload effect to device ({0})", fd);
      return ERROR;
    }
    return (int) ff_effect.VH_id.get(effectSegment, 0);
  }

  /**
   * Remove a force feedback effect from the device.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor of the event device
   * @param effectId    the ID of the effect to remove
   * @return 0 on success, or -1 if an error occurred
   */
  static int removeEffect(Arena memoryArena, int fd, int effectId) {
    int result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCRMFF, effectId);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to remove effect ({0}) from device ({1})", new Object[] {effectId, fd});
      return ERROR;
    }
    return result;
  }

  /**
   * Write an event to the device.
   *
   * <p>
   * This is used to play or stop force feedback effects.
   * </p>
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor of the event device
   * @param event       the event to write
   * @return the number of bytes written, or -1 if an error occurred
   */
  static int writeEvent(Arena memoryArena, int fd, input_event event) {
    var eventSegment = memoryArena.allocate(input_event.$LAYOUT);
    event.write(eventSegment);
    int result = invoke(HANDLE_WRITE, memoryArena, fd, eventSegment, input_event.$LAYOUT.byteSize());
    if (result == ERROR) {
      log.log(Level.WARNING, "Failed to write event to device ({0})", fd);
      return ERROR;
    }
    return result;
  }

  /**
   * Set the force feedback gain for the device.
   *
   * <p>
   * This sets the overall gain for all force feedback effects.
   * The gain is a value from 0 to 65535 representing 0% to 100%.
   * </p>
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor of the event device
   * @param gain        the gain value (0 to 65535)
   * @return 0 on success, or -1 if an error occurred
   */
  static int setGain(Arena memoryArena, int fd, int gain) {
    var gainEvent = new input_event();
    gainEvent.type = (short) LinuxEventDevice.EV_FF;
    gainEvent.code = (short) FF_GAIN;
    gainEvent.value = gain;

    return writeEvent(memoryArena, fd, gainEvent);
  }

  /**
   * Get the bits for the given event type.
   * <p>
   * The bits are used to determine which event types are supported by the device.
   * The bits are stored in a byte array where each bit represents an event type.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param evtype      the event type
   * @param fd          the file descriptor of the event device
   * @return the bits for the given event type or null if an error occurred
   */
  static byte[] getBits(Arena memoryArena, int evtype, int fd) {
    var len = LinuxEventDevice.getMaxBits(evtype) / 8 + 1;

    MemorySegment bitsMemorySegment = memoryArena.allocate(MemoryLayout.sequenceLayout(len, JAVA_BYTE));
    int result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGBIT(evtype, len), bitsMemorySegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get key states for device ({0}) and evtype {1}", new Object[] {fd, evtype});
      return null;
    }

    byte[] bits = new byte[len];
    for (int i = 0; i < len; i++) {
      bits[i] = bitsMemorySegment.get(JAVA_BYTE, i);
    }

    return bits;
  }

  private static int invoke(String handleName, Arena memoryArena, Object... args) {
    var capturedState = memoryArena.allocate(Linker.Option.captureStateLayout());
    var methodHandle = handles.get(handleName);
    if (methodHandle == null) {
      log.log(Level.SEVERE, "Could not find method handle for ''{0}''", handleName);
      return ERROR;
    }

    try {
      var result = ERROR;
      if (args == null || args.length == 0) {
        result = (int) methodHandle.invoke(capturedState);
      } else if (args.length == 1) {
        result = (int) methodHandle.invoke(capturedState, args[0]);
      } else if (args.length == 2) {
        result = (int) methodHandle.invoke(capturedState, args[0], args[1]);
      } else if (args.length == 3) {
        result = (int) methodHandle.invoke(capturedState, args[0], args[1], args[2]);
      } else if (args.length == 4) {
        result = (int) methodHandle.invoke(capturedState, args[0], args[1], args[2], args[3]);
      } else if (args.length == 5) {
        result = (int) methodHandle.invoke(capturedState, args[0], args[1], args[2], args[3], args[4]);
      }

      if (result == ERROR) {
        var errorNo = getErrorNo(capturedState);

        // we are using non-blocking mode, so we can ignore EAGAIN because it is not an error, just a signal that we're done reading
        if(errorNo == EAGAIN) {
          return result;
        }

        log.log(Level.SEVERE, "Could not invoke ''{0}'' - {1}({2})", new Object[] {handleName, getErrorString(errorNo), errorNo});
      }

      return result;
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return ERROR;
  }

  private static int getErrorNo(MemorySegment capturedState) {
    return (int) errnoHandle.get(capturedState, 0);
  }

  private static String getErrorString(int errorNo) {
    try {
      return ((MemorySegment) strerror.invoke(errorNo)).reinterpret(Long.MAX_VALUE).getString(0, Charset.defaultCharset());
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return null;
  }

  /**
   * see ioctl.h for details
   */
  private static int _IOC(int dir, char type, int nr, int size) {
    final int IOC_DIRSHIFT = 30;
    final int IOC_TYPESHIFT = 8;
    final int IOC_NRSHIFT = 0;
    final int IOC_SIZESHIFT = 16;

    return ((dir << IOC_DIRSHIFT) |
            (type << IOC_TYPESHIFT) |
            (nr << IOC_NRSHIFT) |
            (size << IOC_SIZESHIFT));
  }

  private static int _IOR(char type, int nr, long size) {
    return _IOC(_IOC_READ, type, nr, (int) size);
  }

  private static int _IOW(char type, int nr, long size) {
    return _IOC(1, type, nr, (int) size);
  }
}
