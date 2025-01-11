package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.foreign.NativeHelper;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

class Linux {
  private static final Logger log = Logger.getLogger(Linux.class.getName());
  final static int ERROR = -1;

  final static int O_RDONLY = 0;

  // TODO: if we want to rumble, we need to open the device in read/write mode
  final static int O_RDWR = 2;

  final static int _IOC_READ = 2;
  final static int NAME_BUFFER_SIZE = 1024;
  final static String ERRNO = "errno";

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

  private static final MethodHandle open;
  private static final MethodHandle close;
  private static final MethodHandle ioctl;
  private static final MethodHandle read;

  static {
    StructLayout capturedStateLayout = Linker.Option.captureStateLayout();

    errnoHandle = capturedStateLayout.varHandle(MemoryLayout.PathElement.groupElement(ERRNO));
    strerror = NativeHelper.downcallHandle("strerror", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

    open = downcallHandle("open", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT), ERRNO);
    close = downcallHandle("close", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT), ERRNO);
    ioctl = downcallHandle("ioctl", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS), ERRNO);
    read = NativeHelper.downcallHandle("read", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG), ERRNO);
  }

  static int open(Arena memoryArena, String fileName) {
    var filenameMemorySegment = memoryArena.allocateFrom(fileName);
    return invoke(open, memoryArena, filenameMemorySegment, O_RDONLY, null);
  }

  static void close(Arena memoryArena, int fd) {
    invoke(close, memoryArena, fd, null, null);
  }

  static String getEventDeviceName(Arena memoryArena, int fd) {
    var nameMemorySegment = memoryArena.allocateFrom(JAVA_CHAR, new char[NAME_BUFFER_SIZE]);
    var result = invoke(ioctl, memoryArena, fd, EVIOCGNAME, nameMemorySegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get device name for device (" + fd + ")");
      return null;
    }

    return nameMemorySegment.getString(0);
  }

  static int getEventDeviceVersion(Arena memoryArena, int fd) {
    var versionMemorySegment = memoryArena.allocate(JAVA_INT);
    var result = invoke(ioctl, memoryArena, fd, EVIOCGVERSION, versionMemorySegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get device version for device (" + fd + ")");
      return 0;
    }

    return versionMemorySegment.get(JAVA_INT, 0);
  }

  static input_id getEventDeviceId(Arena memoryArena, int fd) {
    var inputIdMemorySegment = memoryArena.allocate(input_id.$LAYOUT);
    var result = invoke(ioctl, memoryArena, fd, EVIOCGID, inputIdMemorySegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get device id for device (" + fd + ")");
      return null;
    }

    return input_id.read(inputIdMemorySegment);
  }

  static input_absinfo getAbsInfo(Arena memoryArena, int fd, int absAxis) {
    MemorySegment absInfoSegment = memoryArena.allocate(input_absinfo.$LAYOUT);
    int result = invoke(ioctl, memoryArena, fd, EVIOCGABS(absAxis), absInfoSegment);
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
    int result = invoke(ioctl, memoryArena, fd, EVIOCGEFFECTS, numEffectsSegment);
    if (result == ERROR) {
      log.log(Level.SEVERE, "Failed to get number of device effects (" + fd + ")");
      return ERROR;
    }
    return numEffectsSegment.get(JAVA_INT, 0);
  }

  static boolean[] getKeyStates(Arena memoryArena, int fd) {
    var len = LinuxEventDevice.KEY_MAX / 8 + 1;

    MemorySegment bitsMemorySegment = memoryArena.allocate(MemoryLayout.sequenceLayout(len, JAVA_BYTE));
    int result = invoke(ioctl, memoryArena, fd, EVIOCGKEY(len), bitsMemorySegment);
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

  static byte[] getBits(Arena memoryArena, int evtype, int fd) {
    var len = LinuxEventDevice.getMaxBits(evtype) / 8 + 1;

    MemorySegment bitsMemorySegment = memoryArena.allocate(MemoryLayout.sequenceLayout(len, JAVA_BYTE));
    int result = invoke(ioctl, memoryArena, fd, EVIOCGBIT(evtype, len), bitsMemorySegment);
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

  public static input_event readEvent(Arena memoryArena, int fd) {
    MemorySegment inputEventMemorySegment = memoryArena.allocate(input_event.$LAYOUT);
    int result = invoke(read, memoryArena, fd, inputEventMemorySegment, input_event.$LAYOUT.byteSize());
    if(result == ERROR) {
      log.log(Level.FINE, "No more events to read from device (" + fd + ")");
      return null;
    }

    return input_event.read(inputEventMemorySegment);
  }

  private static int invoke(MethodHandle methodHandle, Arena memoryArena, Object arg1, Object arg2, Object arg3) {
    var capturedState = memoryArena.allocate(Linker.Option.captureStateLayout());
    try {
      var result = ERROR;
      if (arg1 == null) {
        result = (int) methodHandle.invoke(capturedState);
      } else if (arg2 == null) {
        result = (int) methodHandle.invoke(capturedState, arg1);
      } else if (arg3 == null) {
        result = (int) methodHandle.invoke(capturedState, arg1, arg2);
      } else {
        result = (int) methodHandle.invoke(capturedState, arg1, arg2, arg3);
      }

      if (result == ERROR) {
        var errorNo = getErrorNo(capturedState);
        log.log(Level.SEVERE, "Could not invoke '" + methodHandle + "' - " + getErrorString(errorNo) + "(" + errorNo + ")");
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
