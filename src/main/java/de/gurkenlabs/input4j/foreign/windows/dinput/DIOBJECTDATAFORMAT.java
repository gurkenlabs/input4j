package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

class DIOBJECTDATAFORMAT {
  public MemorySegment pguid;

  public int dwOfs;

  public int dwType;

  public int dwFlags;

  DIOBJECTDATAFORMAT() {
  }

  DIOBJECTDATAFORMAT(MemorySegment pguid, int dwOfs, int dwType, int dwFlags) {
    this.pguid = pguid;
    this.dwOfs = dwOfs;
    this.dwType = dwType;
    this.dwFlags = dwFlags;
  }

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          ADDRESS.withName("pguid"),
          JAVA_INT.withName("dwOfs"),
          JAVA_INT.withName("dwType"),
          JAVA_INT.withName("dwFlags"),
          MemoryLayout.paddingLayout(4)
  ).withName("DIOBJECTDATAFORMAT");

  static final VarHandle VH_pguid = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("pguid"));
  static final VarHandle VH_dwOfs = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwOfs"));
  static final VarHandle VH_dwType = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwType"));
  static final VarHandle VH_dwFlags = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwFlags"));

  public static DIOBJECTDATAFORMAT read(MemorySegment segment) {
    var data = new DIOBJECTDATAFORMAT();

    data.pguid = (MemorySegment) VH_pguid.get(segment, 0);
    data.dwOfs = (int) VH_dwOfs.get(segment, 0);
    data.dwType = (int) VH_dwType.get(segment, 0);
    data.dwFlags = (int) VH_dwFlags.get(segment, 0);

    return data;
  }

  public void write(MemorySegment segment) {
    VH_pguid.set(segment, 0, pguid);
    VH_dwOfs.set(segment, 0, dwOfs);
    VH_dwType.set(segment, 0, dwType);
    VH_dwFlags.set(segment, 0, dwFlags);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (DIOBJECTDATAFORMAT) obj;
    return this.pguid.equals(that.pguid) &&
            this.dwOfs == that.dwOfs &&
            this.dwType == that.dwType &&
            this.dwFlags == that.dwFlags;
  }
}
