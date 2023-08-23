package de.gurkenlabs.litiengine.input.windows.dinput;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

class DIPROPRANGE {
  public DIPROPHEADER diph;

  public int lMin;

  public int lMax;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          DIPROPHEADER.$LAYOUT.withName("diph"),
          JAVA_INT.withName("lMin"),
          JAVA_INT.withName("lMax")
  ).withName("DIPROPRANGE");

  static final VarHandle VH_lMin = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lMin"));
  static final VarHandle VH_lMax = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lMax"));

  public static DIPROPRANGE read(MemorySegment segment) {
    var data = new DIPROPRANGE();
    data.diph = DIPROPHEADER.read(segment);
    data.lMin = (int) VH_lMin.get(segment);
    data.lMax = (int) VH_lMax.get(segment);

    return data;
  }

  public void write(MemorySegment segment) {
    diph.write(segment);
    VH_lMin.set(segment, lMin);
    VH_lMax.set(segment, lMax);
  }
}
