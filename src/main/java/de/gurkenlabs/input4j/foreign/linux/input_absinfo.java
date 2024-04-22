package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * struct input_absinfo - used by EVIOCGABS/EVIOCSABS ioctls
 *
 * <p>
 * Note that input core does not clamp reported values to the
 * [minimum, maximum] limits, such task is left to userspace.
 * <p>
 * The default resolution for main axes (ABS_X, ABS_Y, ABS_Z,
 * ABS_MT_POSITION_X, ABS_MT_POSITION_Y) is reported in units
 * per millimeter (units/mm), resolution for rotational axes
 * (ABS_RX, ABS_RY, ABS_RZ) is reported in units per radian.
 * The resolution for the size axes (ABS_MT_TOUCH_MAJOR,
 * ABS_MT_TOUCH_MINOR, ABS_MT_WIDTH_MAJOR, ABS_MT_WIDTH_MINOR)
 * is reported in units per millimeter (units/mm).
 * When INPUT_PROP_ACCELEROMETER is set the resolution changes.
 * The main axes (ABS_X, ABS_Y, ABS_Z) are then reported in
 * units per g (units/g) and in units per degree per second
 * (units/deg/s) for rotational axes (ABS_RX, ABS_RY, ABS_RZ).
 */
class input_absinfo {
  /**
   * latest reported value for the axis.
   */
  public int value;

  /**
   * specifies minimum value for the axis.
   */
  public int minimum;

  /**
   * specifies maximum value for the axis.
   */
  public int maximum;

  /**
   * specifies fuzz value that is used to filter noise from the event stream.
   */
  public int fuzz;

  /**
   * values that are within this value will be discarded by joydev interface and reported as 0 instead.
   */
  public int flat;

  /**
   * specifies resolution for the values reported for the axis.
   */
  public int resolution;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("value"),
          JAVA_INT.withName("minimum"),
          JAVA_INT.withName("maximum"),
          JAVA_INT.withName("fuzz"),
          JAVA_INT.withName("flat"),
          JAVA_INT.withName("resolution")
  ).withName("input_absinfo");

  static final VarHandle VH_value = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("value"));
  static final VarHandle VH_minimum = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("minimum"));
  static final VarHandle VH_maximum = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("maximum"));
  static final VarHandle VH_fuzz = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("fuzz"));
  static final VarHandle VH_flat = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("flat"));
  static final VarHandle VH_resolution = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("resolution"));

  public static input_absinfo read(MemorySegment segment) {
    var absInfo = new input_absinfo();
    absInfo.value = (int) VH_value.get(segment);
    absInfo.minimum = (int) VH_minimum.get(segment);
    absInfo.maximum = (int) VH_maximum.get(segment);
    absInfo.fuzz = (int) VH_fuzz.get(segment);
    absInfo.flat = (int) VH_flat.get(segment);
    absInfo.resolution = (int) VH_resolution.get(segment);

    return absInfo;
  }

  public void write(MemorySegment segment) {
    VH_value.set(segment, value);
    VH_minimum.set(segment, minimum);
    VH_maximum.set(segment, maximum);
    VH_fuzz.set(segment, fuzz);
    VH_flat.set(segment, flat);
    VH_resolution.set(segment, resolution);
  }
}
