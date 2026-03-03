package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

class timeval {
  /**
   * seconds
   */
  public long tv_sec;

  /**
   * microseconds
   */
  public long tv_usec;

  private static final boolean IS_32_BIT = is32BitSystem();

  private static boolean is32BitSystem() {
    String osArch = System.getProperty("os.arch", "").toLowerCase();
    return osArch.equals("arm") || osArch.equals("i386") || osArch.equals("i486")
        || osArch.equals("i586") || osArch.equals("i686") || osArch.equals("x86")
        || osArch.startsWith("armv");
  }

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          (IS_32_BIT ? JAVA_INT : JAVA_LONG).withName("tv_sec"),
          (IS_32_BIT ? JAVA_INT : JAVA_LONG).withName("tv_usec")
  ).withName("timeval");

  static final VarHandle VH_tv_sec = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("tv_sec"));
  static final VarHandle VH_tv_usec = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("tv_usec"));

  public static timeval read(MemorySegment segment) {
    var timeval = new timeval();
    timeval.tv_sec = (long) VH_tv_sec.get(segment, 0);
    timeval.tv_usec = (long) VH_tv_usec.get(segment, 0);

    return timeval;
  }

  public void write(MemorySegment segment) {
    VH_tv_sec.set(segment, 0, tv_sec);
    VH_tv_usec.set(segment, 0, tv_usec);
  }
}
