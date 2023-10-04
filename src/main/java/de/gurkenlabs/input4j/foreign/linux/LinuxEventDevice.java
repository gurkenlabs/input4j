package de.gurkenlabs.input4j.foreign.linux;

import java.io.Closeable;
import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;

public class LinuxEventDevice implements Closeable {
  private final String filename;
 //  private static final MethodHandle open;
  static {

  }
  public LinuxEventDevice(String filename, Arena memoryArena) {
    this.filename = filename;

    var fileDescriptor = open(memoryArena);
    if (fileDescriptor == -1) {
      // TODO: log sth.
      return;
    }
  }

  private int open(Arena memoryArena) {
    final int O_RDWR = 2;
    final int O_RDONLY = 0;
    var filenameMemorySegment = memoryArena.allocateArray(ValueLayout.JAVA_CHAR, this.filename.toCharArray());
    // TODO
    return -1;
  }

  @Override
  public void close() {

  }
}
