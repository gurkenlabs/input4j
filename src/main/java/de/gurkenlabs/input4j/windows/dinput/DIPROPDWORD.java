package de.gurkenlabs.input4j.windows.dinput;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

class DIPROPDWORD {

  public DIPROPHEADER diph;

  public int dwData;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          DIPROPHEADER.$LAYOUT.withName("diph"),
          JAVA_INT.withName("dwData")
  ).withName("DIPROPDWORD");

  static final VarHandle VH_dwData = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwData"));

  public static DIPROPDWORD read(MemorySegment segment) {
    var data = new DIPROPDWORD();
    data.diph = DIPROPHEADER.read(segment);
    data.dwData = (int) VH_dwData.get(segment);

    return data;
  }

  public void write(MemorySegment segment) {
    diph.write(segment);

    VH_dwData.set(segment, dwData);
  }
}
