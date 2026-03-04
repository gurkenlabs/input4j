package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * struct ff_effect - defines a force feedback effect.
 *
 * <p>
 * This is the main structure used to define force feedback effects
 * for Linux input devices. It contains the effect type, direction,
 * trigger conditions, playback parameters, and effect-specific data.
 * </p>
 *
 * @see ff_rumble_effect
 * @see ff_trigger
 * @see ff_replay
 */
class ff_effect {
  /**
   * Effect type (FF_RUMBLE, FF_CONSTANT, FF_PERIODIC, etc.).
   */
  public short type;

  /**
   * Unique effect ID assigned by the driver.
   * Set to -1 when uploading a new effect.
   */
  public short id;

  /**
   * Direction of the effect (0-360 degrees, encoded as 0xFFFF represent 360 degrees).
   */
  public short direction;

  /**
   * Trigger conditions for the effect.
   */
  public ff_trigger trigger;

  /**
   * Playback scheduling parameters.
   */
  public ff_replay replay;

  /**
   * Effect-specific data (union of different effect types).
   * For rumble effects, use the rumble field.
   */
  public ff_rumble_effect rumble;

  static final int FF_EFFECT_MAX = 24;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
      JAVA_SHORT.withName("type"),
      JAVA_SHORT.withName("id"),
      JAVA_SHORT.withName("direction"),
      ff_trigger.$LAYOUT.withName("trigger"),
      ff_replay.$LAYOUT.withName("replay"),
      MemoryLayout.sequenceLayout(FF_EFFECT_MAX, JAVA_SHORT).withName("u")
  ).withName("ff_effect");

  static final VarHandle VH_type = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("type"));
  static final VarHandle VH_id = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("id"));
  static final VarHandle VH_direction = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("direction"));

  static final long OFFSET_trigger = $LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("trigger"));
  static final long OFFSET_replay = $LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("replay"));
  static final long OFFSET_u = $LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("u"));

  public static ff_effect read(MemorySegment segment) {
    var effect = new ff_effect();
    effect.type = (short) VH_type.get(segment, 0);
    effect.id = (short) VH_id.get(segment, 0);
    effect.direction = (short) VH_direction.get(segment, 0);
    effect.trigger = ff_trigger.read(segment.asSlice(OFFSET_trigger));
    effect.replay = ff_replay.read(segment.asSlice(OFFSET_replay));
    effect.rumble = ff_rumble_effect.read(segment.asSlice(OFFSET_u));
    return effect;
  }

  public void write(MemorySegment segment) {
    VH_type.set(segment, 0, type);
    VH_id.set(segment, 0, id);
    VH_direction.set(segment, 0, direction);

    if (trigger != null) {
      trigger.write(segment.asSlice(OFFSET_trigger));
    }
    if (replay != null) {
      replay.write(segment.asSlice(OFFSET_replay));
    }
    if (rumble != null) {
      rumble.write(segment.asSlice(OFFSET_u));
    }
  }
}
