package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

class fd_set {
  static final int FD_SETSIZE = 1024;
  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          MemoryLayout.sequenceLayout(FD_SETSIZE / 32, JAVA_INT).withName("fds_bits")
  ).withName("fd_set");

  static final VarHandle VH_fds_bits = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("fds_bits"), MemoryLayout.PathElement.sequenceElement());

  public static fd_set read(MemorySegment segment) {
    var fdSet = new fd_set();
    // Read the fds_bits array from the segment
    for (int i = 0; i < FD_SETSIZE / 32; i++) {
      fdSet.fds_bits[i] = (int) VH_fds_bits.get(segment, i);
    }
    return fdSet;
  }

  public void write(MemorySegment segment) {
    // Write the fds_bits array to the segment
    for (int i = 0; i < FD_SETSIZE / 32; i++) {
      VH_fds_bits.set(segment, i, fds_bits[i]);
    }
  }

  public int[] fds_bits = new int[FD_SETSIZE / 32];

  public void FD_ZERO() {
    for (int i = 0; i < FD_SETSIZE / 32; i++) {
      fds_bits[i] = 0;
    }
  }

  public void FD_SET(int fd) {
    int index = fd / 32;
    int bit = fd % 32;
    fds_bits[index] |= (1 << bit);
  }
}