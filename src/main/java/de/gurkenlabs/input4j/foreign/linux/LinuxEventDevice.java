package de.gurkenlabs.input4j.foreign.linux;

import java.io.Closeable;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

class LinuxEventDevice implements Closeable {
  final static int ERROR = -1;
  final static int O_RDWR = 2;
  final static int O_RDONLY = 0;
  final static int _IOC_READ = 2;
  final static int BUFFER_SIZE = 1024;

  final static int EVIOCGNAME = _IOC(_IOC_READ, 'E', 0x06, BUFFER_SIZE);

  private static final Logger log = Logger.getLogger(LinuxEventDevice.class.getName());
  private final String filename;

  private final int fileDescriptor;

  private boolean hasReadWriteAccess = true;

  private String deviceName;

  private static final MethodHandle open;

  private static final MethodHandle ioctl;


  static {
    open = downcallHandle("open",
            FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));

    ioctl = downcallHandle("ioctl",
            FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS),
            "errno");
  }

  public LinuxEventDevice(String filename, NativeContext nativeContext) {
    this.filename = filename;

    this.fileDescriptor = open(nativeContext);
    if (this.fileDescriptor == ERROR) {
      log.log(Level.SEVERE, "Could not open linux event device '" + filename + "'");
      return;
    }

    getName(nativeContext);
  }

  private int open(NativeContext nativeContext) {
    var filenameMemorySegment = nativeContext.getArena().allocateArray(ValueLayout.JAVA_CHAR, this.filename.toCharArray());

    var fileDescriptor = ERROR;
    try {
      fileDescriptor = (int) open.invoke(filenameMemorySegment, O_RDWR);
      if (fileDescriptor == ERROR) {
        hasReadWriteAccess = false;
        fileDescriptor = (int) open.invoke(filenameMemorySegment, O_RDONLY);
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return fileDescriptor;
  }

  private void getName(NativeContext nativeContext) {

    var nameMemorySegment = nativeContext.getArena().allocateArray(JAVA_CHAR, new char[BUFFER_SIZE]);

    try {
      // TOOD: fix errno: 25 -- Inappropriate ioctl for device (result from the capturing below)

      var result = (int) ioctl.invoke(nativeContext.getCapturedState(), this.fileDescriptor, EVIOCGNAME, nameMemorySegment);
      if (result == ERROR) {
        log.log(Level.SEVERE, "Could not get name for '" + filename + "'. errno: " + nativeContext.getErrorNo() + " -- " + nativeContext.getError());
        return;
      }

      this.deviceName = nameMemorySegment.getUtf8String(0);
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  @Override
  public void close() {

  }

  public String getFilename() {
    return filename;
  }

  public String getDeviceName() {
    return deviceName;
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
}
