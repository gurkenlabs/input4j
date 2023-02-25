package de.gurkenlabs.litiengine.input.windows;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

class DIOBJECTDATAFORMAT {
  public MemoryAddress pguid;

  public int dwOfs;

  public int dwType;

  public int dwFlags;

  DIOBJECTDATAFORMAT() {
  }

  DIOBJECTDATAFORMAT(MemoryAddress pguid, int dwOfs, int dwType, int dwFlags) {
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
          MemoryLayout.paddingLayout(32)
  ).withName("DIOBJECTDATAFORMAT");

  static final VarHandle VH_pguid = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("pguid"));
  static final VarHandle VH_dwOfs = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwOfs"));
  static final VarHandle VH_dwType = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwType"));
  static final VarHandle VH_dwFlags = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwFlags"));

  public static DIOBJECTDATAFORMAT read(MemorySegment segment) {
    var data = new DIOBJECTDATAFORMAT();

    data.pguid = (MemoryAddress) VH_pguid.get(segment);
    data.dwOfs = (int) VH_dwOfs.get(segment);
    data.dwType = (int) VH_dwType.get(segment);
    data.dwFlags = (int) VH_dwFlags.get(segment);

    return data;
  }

  public void write(MemorySegment segment) {
    VH_pguid.set(segment, pguid);
    VH_dwOfs.set(segment, dwOfs);
    VH_dwType.set(segment, dwType);
    VH_dwFlags.set(segment, dwFlags);
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
