package de.gurkenlabs.litiengine.input.windows;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

class DIOBJECTDATAFORMAT {
  public GUID pguid;

  public int dwOfs;

  public int dwType;

  public int dwFlags;
  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          GUID.$LAYOUT.withName("pguid"),
          JAVA_INT.withName("dwOfs"),
          JAVA_INT.withName("dwType"),
          JAVA_INT.withName("dwFlags"),
          MemoryLayout.paddingLayout(32)
  ).withName("_DIOBJECTDATAFORMAT");

  static final VarHandle VH_dwOfs = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwOfs"));
  static final VarHandle VH_dwType = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwType"));
  static final VarHandle VH_dwFlags = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwFlags"));

  public static DIOBJECTDATAFORMAT read(MemorySegment segment) {
    var data = new DIOBJECTDATAFORMAT();

    data.pguid = GUID.read(segment);
    data.dwOfs = (int) VH_dwOfs.get(segment);
    data.dwType = (int) VH_dwType.get(segment);
    data.dwFlags = (int) VH_dwFlags.get(segment);

    return data;
  }

  public void write(MemorySegment segment) {
    if (pguid != null) {
      pguid.write(segment);

      VH_dwOfs.set(segment, dwOfs);
      VH_dwType.set(segment, dwType);
      VH_dwFlags.set(segment, dwFlags);
    }
  }
}
