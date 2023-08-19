package de.gurkenlabs.litiengine.input.windows;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

class DIDEVICEOBJECTDATA {
  public int dwOfs;
  public int dwData;
  public int dwTimeStamp;
  public int dwSequence;
  public long uAppData;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("dwOfs"),
          JAVA_INT.withName("dwData"),
          JAVA_INT.withName("dwTimeStamp"),
          JAVA_INT.withName("dwSequence"),
          JAVA_LONG.withName("uAppData")
  ).withName("DIDEVICEOBJECTDATA");

  static final VarHandle VH_dwOfs = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwOfs"));
  static final VarHandle VH_dwData = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwData"));
  static final VarHandle VH_dwTimeStamp = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwTimeStamp"));
  static final VarHandle VH_dwSequence = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSequence"));
  static final VarHandle VH_uAppData = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("uAppData"));

  public static DIDEVICEOBJECTDATA read(MemorySegment segment) {
    var data = new DIDEVICEOBJECTDATA();
    data.dwOfs = (int) VH_dwOfs.get(segment);
    data.dwData = (int) VH_dwData.get(segment);
    data.dwTimeStamp = (int) VH_dwTimeStamp.get(segment);
    data.dwSequence = (int) VH_dwSequence.get(segment);
    data.uAppData = (long) VH_uAppData.get(segment);
    return data;
  }
}
