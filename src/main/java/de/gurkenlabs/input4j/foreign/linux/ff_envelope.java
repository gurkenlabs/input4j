package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * struct ff_envelope - generic force-feedback effect envelope.
 *
 * <p>
 * Defines the attack and fade characteristics of a force-feedback effect.
 * Attack is the ramp-up at the beginning, fade is the ramp-down at the end.
 * </p>
 */
class ff_envelope {
  public short attack_length;
  public short attack_level;
  public short fade_length;
  public short fade_level;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
      JAVA_SHORT.withName("attack_length"),
      JAVA_SHORT.withName("attack_level"),
      JAVA_SHORT.withName("fade_length"),
      JAVA_SHORT.withName("fade_level")
  ).withName("ff_envelope");

  static final VarHandle VH_attack_length = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("attack_length"));
  static final VarHandle VH_attack_level = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("attack_level"));
  static final VarHandle VH_fade_length = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("fade_length"));
  static final VarHandle VH_fade_level = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("fade_level"));

  public static ff_envelope read(MemorySegment segment) {
    var envelope = new ff_envelope();
    envelope.attack_length = (short) VH_attack_length.get(segment, 0);
    envelope.attack_level = (short) VH_attack_level.get(segment, 0);
    envelope.fade_length = (short) VH_fade_length.get(segment, 0);
    envelope.fade_level = (short) VH_fade_level.get(segment, 0);
    return envelope;
  }

  public void write(MemorySegment segment) {
    VH_attack_length.set(segment, 0, attack_length);
    VH_attack_level.set(segment, 0, attack_level);
    VH_fade_length.set(segment, 0, fade_length);
    VH_fade_level.set(segment, 0, fade_level);
  }
}
