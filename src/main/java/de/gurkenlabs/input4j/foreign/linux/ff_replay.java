package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * struct ff_replay - defines scheduling of a force feedback effect.
 *
 * <p>
 * This structure is used to define the timing of a force feedback effect
 * when it should be played.
 * </p>
 */
class ff_replay {
  /**
   * Duration of the effect in milliseconds.
   */
  public short length;

  /**
   * Delay before playing the effect in milliseconds.
   */
  public short delay;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
      JAVA_SHORT.withName("length"),
      JAVA_SHORT.withName("delay")
  ).withName("ff_replay");

  static final VarHandle VH_length = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("length"));
  static final VarHandle VH_delay = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("delay"));

  public static ff_replay read(MemorySegment segment) {
    var replay = new ff_replay();
    replay.length = (short) VH_length.get(segment, 0);
    replay.delay = (short) VH_delay.get(segment, 0);
    return replay;
  }

  public void write(MemorySegment segment) {
    VH_length.set(segment, 0, length);
    VH_delay.set(segment, 0, delay);
  }
}
