package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * struct ff_trigger - defines when a force feedback effect is played.
 *
 * <p>
 * This structure defines the button that triggers the effect and
 * the interval for automatic repeat.
 * </p>
 */
class ff_trigger {
  /**
   * Button that triggers the effect. Set to 0xFFFF to disable triggering.
   */
  public short button;

  /**
   * Time between automatic repeats in milliseconds.
   */
  public short interval;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
      JAVA_SHORT.withName("button"),
      JAVA_SHORT.withName("interval")
  ).withName("ff_trigger");

  static final VarHandle VH_button = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("button"));
  static final VarHandle VH_interval = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("interval"));

  public static ff_trigger read(MemorySegment segment) {
    var trigger = new ff_trigger();
    trigger.button = (short) VH_button.get(segment, 0);
    trigger.interval = (short) VH_interval.get(segment, 0);
    return trigger;
  }

  public void write(MemorySegment segment) {
    VH_button.set(segment, 0, button);
    VH_interval.set(segment, 0, interval);
  }
}
