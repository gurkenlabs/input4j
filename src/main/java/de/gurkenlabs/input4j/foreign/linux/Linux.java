package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

class Linux {
  private static final Logger log = Logger.getLogger(Linux.class.getName());
  final static int ERROR = -1;

  final static int O_RDONLY = 0;
  final static int O_RDWR = 2;

  final static int _IOC_READ = 2;
  final static int NAME_BUFFER_SIZE = 1024;

  private final static int EVIOCGVERSION = _IOR('E', 0x01, JAVA_INT.byteSize());
  private final static int EVIOCGID = _IOR('E', 0x02, input_id.$LAYOUT.byteSize());
  private final static int EVIOCGNAME = _IOC(_IOC_READ, 'E', 0x06, NAME_BUFFER_SIZE);


  private static final MethodHandle open;

  private static final MethodHandle ioctl;

  static {
    open = downcallHandle("open",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));

    ioctl = downcallHandle("ioctl",
            FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS),
            "errno");
  }

  static int open(NativeContext nativeContext, String fileName, int flags) {
    var filenameMemorySegment = nativeContext.getArena().allocateArray(ValueLayout.JAVA_CHAR, fileName.toCharArray());

    var fileDescriptor = ERROR;
    try {
      fileDescriptor = (int) open.invoke(filenameMemorySegment, flags);
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return fileDescriptor;
  }

  static String getEventDeviceName(NativeContext nativeContext, int fileDescriptor) {
    var nameMemorySegment = nativeContext.getArena().allocateArray(JAVA_CHAR, new char[NAME_BUFFER_SIZE]);
    try {
      // TOOD: fix errno: 25 -- Inappropriate ioctl for device
      var result = (int) ioctl.invoke(nativeContext.getCapturedState(), fileDescriptor, EVIOCGNAME, nameMemorySegment);
      if (result == ERROR) {
        log.log(Level.SEVERE, "Could not get name for event device '" + fileDescriptor + "' - " + nativeContext.getError() + "(" + nativeContext.getErrorNo() + ")");
        return null;
      }

      return nameMemorySegment.getUtf8String(0);
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }

  static int getEventDeviceVersion(NativeContext nativeContext, int fileDescriptor) {
    var versionMemorySegment = nativeContext.getArena().allocate(JAVA_INT);

    try {
      var result = (int) ioctl.invoke(nativeContext.getCapturedState(), fileDescriptor, EVIOCGVERSION, versionMemorySegment);
      if (result == ERROR) {
        log.log(Level.SEVERE, "Could not get version for event device '" + fileDescriptor + "' - " + nativeContext.getError() + "(" + nativeContext.getErrorNo() + ")");
        return 0;
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return versionMemorySegment.get(JAVA_INT, 0);
  }

  static input_id getEventDeviceId(NativeContext nativeContext, int fileDescriptor) {
    var inputIdMemorySegment = nativeContext.getArena().allocate(input_id.$LAYOUT);

    try {
      var result = (int) ioctl.invoke(nativeContext.getCapturedState(), fileDescriptor, EVIOCGID, inputIdMemorySegment);
      if (result == ERROR) {
        log.log(Level.SEVERE, "Could not get id for event device '" + fileDescriptor + "' - " + nativeContext.getError() + "(" + nativeContext.getErrorNo() + ")");
        return null;
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return input_id.read(inputIdMemorySegment);
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
