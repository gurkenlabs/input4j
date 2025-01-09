package de.gurkenlabs.input4j.foreign.windows.xinput;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * The {@code XINPUT_VIBRATION} class represents the vibration settings of an XInput device.
 * It includes information about the left and right motor speeds.
 */
final class XINPUT_VIBRATION {
  /**
   * The maximum value for motor speed.
   */
  static final int MAX_VIBRATION = 65535;

  /**
   * The speed of the left motor.
   * Value range: 0 to 65535.
   */
  short wLeftMotorSpeed;

  /**
   * The speed of the right motor.
   * Value range: 0 to 65535.
   */
  short wRightMotorSpeed;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_SHORT.withName("wLeftMotorSpeed"),
          ValueLayout.JAVA_SHORT.withName("wRightMotorSpeed")
  );

  private static final VarHandle VH_wLeftMotorSpeed = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("wLeftMotorSpeed"));
  private static final VarHandle VH_wRightMotorSpeed = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("wRightMotorSpeed"));

  /**
   * Reads the {@code XINPUT_VIBRATION} from the given memory segment.
   *
   * @param segment The memory segment to read from.
   * @return The {@code XINPUT_VIBRATION} instance.
   */
  static XINPUT_VIBRATION read(MemorySegment segment) {
    var vibration = new XINPUT_VIBRATION();
    vibration.wLeftMotorSpeed = (short) VH_wLeftMotorSpeed.get(segment, 0);
    vibration.wRightMotorSpeed = (short) VH_wRightMotorSpeed.get(segment, 0);
    return vibration;
  }

  /**
   * Writes the {@code XINPUT_VIBRATION} to the given memory segment.
   *
   * @param segment The memory segment to write to.
   */
  void write(MemorySegment segment) {
    VH_wLeftMotorSpeed.set(segment, 0, wLeftMotorSpeed);
    VH_wRightMotorSpeed.set(segment, 0, wRightMotorSpeed);
  }
}