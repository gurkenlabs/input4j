package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/**
 * struct ff_rumble_effect - parameters for a rumble force feedback effect.
 *
 * <p>
 * This structure defines the intensity parameters for a rumble effect.
 * Rumble effects simulate gamepad controller vibration with two motors:
 * a strong motor for heavy vibrations and a weak motor for light vibrations.
 * </p>
 */
class ff_rumble_effect {
  /**
   * Magnitude of the strong motor (0-65535).
   * The strong motor is typically the larger weight in a gamepad.
   */
  public short strong_magnitude;

  /**
   * Magnitude of the weak motor (0-65535).
   * The weak motor is typically the smaller weight in a gamepad.
   */
  public short weak_magnitude;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
      JAVA_SHORT.withName("strong_magnitude"),
      JAVA_SHORT.withName("weak_magnitude")
  ).withName("ff_rumble_effect");

  static final VarHandle VH_strong_magnitude = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("strong_magnitude"));
  static final VarHandle VH_weak_magnitude = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("weak_magnitude"));

  public static ff_rumble_effect read(MemorySegment segment) {
    var effect = new ff_rumble_effect();
    effect.strong_magnitude = (short) VH_strong_magnitude.get(segment, 0);
    effect.weak_magnitude = (short) VH_weak_magnitude.get(segment, 0);
    return effect;
  }

  public void write(MemorySegment segment) {
    VH_strong_magnitude.set(segment, 0, strong_magnitude);
    VH_weak_magnitude.set(segment, 0, weak_magnitude);
  }
}
