package de.gurkenlabs.input4j.foreign.windows.xinput;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * The {@code XINPUT_GAMEPAD} class represents the state of an XInput gamepad.
 * It includes information about the buttons, triggers, and thumbsticks of the gamepad.
 */
final class XINPUT_GAMEPAD {
  static final int XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE = 7849;
  static final int XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE = 8689;
  static final int XINPUT_GAMEPAD_TRIGGER_THRESHOLD = 30;

  /**
   * The buttons pressed on the gamepad.
   * Potential values:
   * <ul>
   *   <li>{@code 0x0001}: D-pad Up</li>
   *   <li>{@code 0x0002}: D-pad Down</li>
   *   <li>{@code 0x0004}: D-pad Left</li>
   *   <li>{@code 0x0008}: D-pad Right</li>
   *   <li>{@code 0x0010}: Start</li>
   *   <li>{@code 0x0020}: Back</li>
   *   <li>{@code 0x0040}: Left Thumb</li>
   *   <li>{@code 0x0080}: Right Thumb</li>
   *   <li>{@code 0x0100}: Left Shoulder</li>
   *   <li>{@code 0x0200}: Right Shoulder</li>
   *   <li>{@code 0x1000}: A</li>
   *   <li>{@code 0x2000}: B</li>
   *   <li>{@code 0x4000}: X</li>
   *   <li>{@code 0x8000}: Y</li>
   * </ul>
   */
  short wButtons;

  /**
   * Represents the pressure applied to the left trigger.
   * Value range: 0 to 255.
   */
  byte bLeftTrigger;

  /**
   * Represents the pressure applied to the right trigger.
   * Value range: 0 to 255.
   */
  byte bRightTrigger;

  /**
   * Represents the position of the left thumbstick along the X-axis.
   * Value range: -32768 to 32767.
   */
  short sThumbLX;

  /**
   * Represents the position of the left thumbstick along the Y-axis.
   * Value range: -32768 to 32767.
   */
  short sThumbLY;

  /**
   * Represents the position of the right thumbstick along the X-axis.
   * Value range: -32768 to 32767.
   */
  short sThumbRX;

  /**
   * Represents the position of the right thumbstick along the Y-axis.
   * Value range: -32768 to 32767.
   */
  short sThumbRY;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_SHORT.withName("wButtons"),
          ValueLayout.JAVA_BYTE.withName("bLeftTrigger"),
          ValueLayout.JAVA_BYTE.withName("bRightTrigger"),
          ValueLayout.JAVA_SHORT.withName("sThumbLX"),
          ValueLayout.JAVA_SHORT.withName("sThumbLY"),
          ValueLayout.JAVA_SHORT.withName("sThumbRX"),
          ValueLayout.JAVA_SHORT.withName("sThumbRY")
  );

  private static final VarHandle VH_wButtons = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("wButtons"));
  private static final VarHandle VH_bLeftTrigger = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("bLeftTrigger"));
  private static final VarHandle VH_bRightTrigger = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("bRightTrigger"));
  private static final VarHandle VH_sThumbLX = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("sThumbLX"));
  private static final VarHandle VH_sThumbLY = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("sThumbLY"));
  private static final VarHandle VH_sThumbRX = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("sThumbRX"));
  private static final VarHandle VH_sThumbRY = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("sThumbRY"));

  /**
   * Reads the {@code XINPUT_GAMEPAD} from the given memory segment.
   *
   * @param segment The memory segment to read from.
   * @return The {@code XINPUT_GAMEPAD} instance.
   */
  static XINPUT_GAMEPAD read(MemorySegment segment) {
    var gamepad = new XINPUT_GAMEPAD();
    gamepad.wButtons = (short) VH_wButtons.get(segment, 0);
    gamepad.bLeftTrigger = (byte) VH_bLeftTrigger.get(segment, 0);
    gamepad.bRightTrigger = (byte) VH_bRightTrigger.get(segment, 0);
    gamepad.sThumbLX = (short) VH_sThumbLX.get(segment, 0);
    gamepad.sThumbLY = (short) VH_sThumbLY.get(segment, 0);
    gamepad.sThumbRX = (short) VH_sThumbRX.get(segment, 0);
    gamepad.sThumbRY = (short) VH_sThumbRY.get(segment, 0);
    return gamepad;
  }

  /**
   * Writes the {@code XINPUT_GAMEPAD} to the given memory segment.
   *
   * @param segment The memory segment to write to.
   */
  void write(MemorySegment segment) {
    VH_wButtons.set(segment, 0, wButtons);
    VH_bLeftTrigger.set(segment, 0, bLeftTrigger);
    VH_bRightTrigger.set(segment, 0, bRightTrigger);
    VH_sThumbLX.set(segment, 0, sThumbLX);
    VH_sThumbLY.set(segment, 0, sThumbLY);
    VH_sThumbRX.set(segment, 0, sThumbRX);
    VH_sThumbRY.set(segment, 0, sThumbRY);
  }
}
