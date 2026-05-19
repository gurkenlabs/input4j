package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * struct ff_periodic_effect - parameters for a periodic force-feedback effect.
 *
 * <p>
 * Defines a periodic waveform effect (sine, square, triangle, sawtooth).
 * Used as a fallback for rumble on devices that support FF_PERIODIC but not FF_RUMBLE.
 * </p>
 */
class ff_periodic_effect {
  public short waveform;
  public short period;
  public short magnitude;
  public short offset;
  public short phase;
  public ff_envelope envelope;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
      JAVA_SHORT.withName("waveform"),
      JAVA_SHORT.withName("period"),
      JAVA_SHORT.withName("magnitude"),
      JAVA_SHORT.withName("offset"),
      JAVA_SHORT.withName("phase"),
      ff_envelope.$LAYOUT.withName("envelope")
  ).withName("ff_periodic_effect");

  static final VarHandle VH_waveform = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("waveform"));
  static final VarHandle VH_period = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("period"));
  static final VarHandle VH_magnitude = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("magnitude"));
  static final VarHandle VH_offset = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("offset"));
  static final VarHandle VH_phase = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("phase"));

  static final long OFFSET_envelope = $LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("envelope"));

  public static ff_periodic_effect read(MemorySegment segment) {
    var effect = new ff_periodic_effect();
    effect.waveform = (short) VH_waveform.get(segment, 0);
    effect.period = (short) VH_period.get(segment, 0);
    effect.magnitude = (short) VH_magnitude.get(segment, 0);
    effect.offset = (short) VH_offset.get(segment, 0);
    effect.phase = (short) VH_phase.get(segment, 0);
    effect.envelope = ff_envelope.read(segment.asSlice(OFFSET_envelope));
    return effect;
  }

  public void write(MemorySegment segment) {
    VH_waveform.set(segment, 0, waveform);
    VH_period.set(segment, 0, period);
    VH_magnitude.set(segment, 0, magnitude);
    VH_offset.set(segment, 0, offset);
    VH_phase.set(segment, 0, phase);
    if (envelope != null) {
      envelope.write(segment.asSlice(OFFSET_envelope));
    }
  }
}
