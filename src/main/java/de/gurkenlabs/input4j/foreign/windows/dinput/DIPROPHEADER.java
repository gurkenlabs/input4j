package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

class DIPROPHEADER {
  static final int DIPH_DEVICE = 0;
  static final int DIPH_BYOFFSET = 1;
  static final int DIPH_BYID = 2;
  static final int DIPH_BYUSAGE = 3;

  public int dwSize;
  public int dwHeaderSize = (int) $LAYOUT.byteSize();
  public int dwObj;
  public int dwHow;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("dwSize"),
          JAVA_INT.withName("dwHeaderSize"),
          JAVA_INT.withName("dwObj"),
          JAVA_INT.withName("dwHow")
  ).withName("DIPROPHEADER");

  static final VarHandle VH_dwSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSize"));
  static final VarHandle VH_dwHeaderSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwHeaderSize"));
  static final VarHandle VH_dwObj = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwObj"));
  static final VarHandle VH_dwHow = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwHow"));

  public static DIPROPHEADER read(MemorySegment segment) {
    var data = new DIPROPHEADER();
    data.dwSize = (int) VH_dwSize.get(segment);
    data.dwHeaderSize = (int) VH_dwHeaderSize.get(segment);
    data.dwObj = (int) VH_dwObj.get(segment);
    data.dwHow = (int) VH_dwHow.get(segment);

    return data;
  }

  public void write(MemorySegment segment) {
    VH_dwSize.set(segment, dwSize);
    VH_dwHeaderSize.set(segment, dwHeaderSize);
    VH_dwObj.set(segment, dwObj);
    VH_dwHow.set(segment, dwHow);
  }
}
