package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

/**
 * Describes the type-specific parameters for a constant force effect.
 */
final class DICONSTANTFORCE {
  /**
   * Magnitude of the effect, in the range from - 10000 through 10000.
   */
  public int lMagnitude;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("lMagnitude")
  ).withName("DICONSTANTFORCE");

  static final VarHandle VH_lMagnitude = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lMagnitude"));

  public static DICONSTANTFORCE read(MemorySegment segment) {
    var data = new DICONSTANTFORCE();
    data.lMagnitude = (int) VH_lMagnitude.get(segment, 0);
    return data;
  }

  public static void write(MemorySegment segment, DICONSTANTFORCE data) {
    VH_lMagnitude.set(segment, 0, data.lMagnitude);
  }
}
