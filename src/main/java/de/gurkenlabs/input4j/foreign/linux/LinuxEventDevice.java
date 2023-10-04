package de.gurkenlabs.input4j.foreign.linux;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

class LinuxEventDevice implements Closeable {
  final static int ERROR = -1;
  final static int O_RDWR = 2;
  final static int O_RDONLY = 0;

  private static final Logger log = Logger.getLogger(LinuxEventDevice.class.getName());
  private final String filename;

  private boolean hasReadWriteAccess = true;

  private static final MethodHandle open;

  static {
    open = downcallHandle("open",
            FunctionDescriptor.of(ADDRESS, JAVA_INT));
  }

  public LinuxEventDevice(String filename, Arena memoryArena) {
    this.filename = filename;

    var fileDescriptor = open(memoryArena);
    if (fileDescriptor == ERROR) {
      log.log(Level.SEVERE, "Could not open linux event device " + filename);
      return;
    }
  }

  private int open(Arena memoryArena) {
    var filenameMemorySegment = memoryArena.allocateArray(ValueLayout.JAVA_CHAR, this.filename.toCharArray());

    var fileDescriptor = ERROR;
    try {
      // TODO
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

  @Override
  public void close() {

  }

  public String getFilename() {
    return filename;
  }
}
