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
  private static final Logger log = Logger.getLogger(Linux.class.getName());
  final static int ERROR = -1;

  final static int O_RDONLY = 0;
  final static int O_NONBLOCK = 0x1000;
  final static int EPOLL_CTL_ADD = 1;
  final static int EPOLL_CTL_DEL = 2;
  final static int EPOLL_CTL_MOD = 3;
  final static int EPOLLIN = 0x001;
  final static int EPOLL_CLOEXEC = 0x80000;

  // TODO: if we want to rumble, we need to open the device in read/write mode
  final static int O_RDWR = 2;

  final static int _IOC_READ = 2;
  final static int NAME_BUFFER_SIZE = 1024;
  final static String ERRNO = "errno";
  final static String HANDLE_STRERROR = "strerror";
  final static String HANDLE_OPEN = "open";
  final static String HANDLE_CLOSE = "close";
  final static String HANDLE_IOCTL = "ioctl";
  final static String HANDLE_READ = "read";
  final static String HANDLE_SELECT = "select";
  final static String HANDLE_EPOLL_CREATE = "epoll_create1";
  final static String HANDLE_EPOLL_CTL = "epoll_ctl";
  final static String HANDLE_EPOLL_WAIT = "epoll_wait";

  private final static int EVIOCGVERSION = _IOR('E', 0x01, JAVA_INT.byteSize());
  private final static int EVIOCGID = _IOR('E', 0x02, input_id.$LAYOUT.byteSize());
  private final static int EVIOCGNAME = _IOC(_IOC_READ, 'E', 0x06, NAME_BUFFER_SIZE);
  private static final int EVIOCGEFFECTS = _IOR('E', 0x84, JAVA_INT.byteSize());

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

    handles.put(HANDLE_OPEN, downcallHandle(HANDLE_OPEN, FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT), ERRNO));
    handles.put(HANDLE_CLOSE, downcallHandle(HANDLE_CLOSE, FunctionDescriptor.of(JAVA_INT, JAVA_INT), ERRNO));
    handles.put(HANDLE_IOCTL, downcallHandle(HANDLE_IOCTL, FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS), ERRNO));
    handles.put(HANDLE_READ, downcallHandle(HANDLE_READ, FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, JAVA_LONG), ERRNO));
    handles.put(HANDLE_SELECT, downcallHandle(HANDLE_SELECT, FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_LONG), ERRNO));

    handles.put(HANDLE_EPOLL_CREATE, downcallHandle(HANDLE_EPOLL_CREATE, FunctionDescriptor.of(JAVA_INT, JAVA_INT), ERRNO));
    handles.put(HANDLE_EPOLL_CTL, downcallHandle(HANDLE_EPOLL_CTL, FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS), ERRNO));
    handles.put(HANDLE_EPOLL_WAIT, downcallHandle(HANDLE_EPOLL_WAIT, FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT), ERRNO));
  }

  static int epollCreate(Arena memoryArena) {
    return invoke(HANDLE_EPOLL_CREATE, memoryArena, EPOLL_CLOEXEC);
  }

  static int epollCtl(Arena memoryArena, int epfd, int fd) {
    var event = new epoll_event();
    event.events = Linux.EPOLLIN;
    event.data_fd = fd;

    MemorySegment eventMemorySegment = memoryArena.allocate(epoll_event.$LAYOUT);
    event.write(eventMemorySegment);
    return invoke(HANDLE_EPOLL_CTL, memoryArena, epfd, EPOLL_CTL_ADD, fd, eventMemorySegment);
  }

  static int epollWait(Arena memoryArena, int epfd, int maxevents) {
    MemorySegment eventsMemorySegment = memoryArena.allocate(MemoryLayout.sequenceLayout(maxevents, JAVA_INT));

    return invoke(HANDLE_EPOLL_WAIT, memoryArena, epfd, eventsMemorySegment, maxevents, -1);
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
   * Close the file descriptor.
   *
   * @param memoryArena the memory arena to allocate memory from
   * @param fd          the file descriptor to close
   */
  static void close(Arena memoryArena, int fd) {
    invoke(HANDLE_CLOSE, memoryArena, fd);
  }

  static int select(Arena memoryArena, int fd, long timeout) {

    var timevalMemorySegment = memoryArena.allocate(timeval.$LAYOUT);
    new timeval().write(timevalMemorySegment);

    var fdSegment = memoryArena.allocate(JAVA_INT);
    fdSegment.set(JAVA_INT, 0, fd);

    return invoke(HANDLE_SELECT, memoryArena, fd + 1, fdSegment, null, null, timevalMemorySegment);
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
      log.log(Level.FINE, "No more events to read from device (" + fd + ")");
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
      log.log(Level.SEVERE, "Failed to get device name for device (" + fd + ")");
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
      log.log(Level.SEVERE, "Failed to get device version for device (" + fd + ")");
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
      log.log(Level.SEVERE, "Failed to get device id for device (" + fd + ")");
      return null;
    }

    return input_id.read(inputIdMemorySegment);
  }

  static input_absinfo getAbsInfo(Arena memoryArena, int fd, int absAxis) {
    MemorySegment absInfoSegment = memoryArena.allocate(input_absinfo.$LAYOUT);
    int result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGABS(absAxis), absInfoSegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get abs info for axis (" + absAxis + ")");
      return null;
    }

    var absInfo = new input_absinfo();
    absInfo.value = (int) input_absinfo.VH_value.get(absInfoSegment, 0);
    absInfo.minimum = (int) input_absinfo.VH_minimum.get(absInfoSegment, 0);
    absInfo.maximum = (int) input_absinfo.VH_maximum.get(absInfoSegment, 0);
    absInfo.fuzz = (int) input_absinfo.VH_fuzz.get(absInfoSegment, 0);
    absInfo.flat = (int) input_absinfo.VH_flat.get(absInfoSegment, 0);

    return absInfo;
  }

  static int getNumEffects(Arena memoryArena, int fd) {
    MemorySegment numEffectsSegment = memoryArena.allocate(JAVA_INT);
    int result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGEFFECTS, numEffectsSegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get number of device effects (" + fd + ")");
      return ERROR;
    }
    return numEffectsSegment.get(JAVA_INT, 0);
  }

  static boolean[] getKeyStates(Arena memoryArena, int fd) {
    var len = LinuxEventDevice.KEY_MAX / 8 + 1;

    MemorySegment bitsMemorySegment = memoryArena.allocate(MemoryLayout.sequenceLayout(len, JAVA_BYTE));
    int result = invoke(HANDLE_IOCTL, memoryArena, fd, EVIOCGKEY(len), bitsMemorySegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get key states for device (" + fd + ")");
      return null;
    }

    byte[] bits = new byte[len];
    for (int i = 0; i < len; i++) {
      bits[i] = bitsMemorySegment.get(JAVA_BYTE, i);
    }

    boolean[] keyStates = new boolean[LinuxEventDevice.KEY_MAX];

    for (int i = 0; i < keyStates.length; i++) {
      if (LinuxEventDevice.isBitSet(bits, i)) {
        keyStates[i] = true;
      }
    }

    return keyStates;
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
      log.log(Level.SEVERE, "Failed to get key states for device (" + fd + ") and evtype " + evtype);
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
      log.log(Level.SEVERE, "Could not find method handle for '" + handleName + "'");
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
        log.log(Level.SEVERE, "Could not invoke '" + handleName + "' - " + getErrorString(errorNo) + "(" + errorNo + ")");
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
}
