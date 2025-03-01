package de.gurkenlabs.input4j.foreign.windows.xinput;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * The {@code XINPUT_STATE} class represents the state of an XInput device.
 * It includes information about the packet number and the gamepad state.
 */
final class XINPUT_STATE {
  /**
   * The packet number of the XInput device.
   */
  int dwPacketNumber;

  /**
   * The gamepad state of the XInput device.
   */
  XINPUT_GAMEPAD Gamepad;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_INT.withName("dwPacketNumber"),
          XINPUT_GAMEPAD.$LAYOUT.withName("Gamepad")
  );

  private static final VarHandle VH_dwPacketNumber = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("dwPacketNumber"));
  private static final long GamepadOffset = $LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Gamepad"));

  /**
   * Reads the {@code XINPUT_STATE} from the given memory segment.
   *
   * @param segment The memory segment to read from.
   * @return The {@code XINPUT_STATE} instance.
   */
  static XINPUT_STATE read(MemorySegment segment) {
    var state = new XINPUT_STATE();
    state.dwPacketNumber = (int) VH_dwPacketNumber.get(segment, 0);
    state.Gamepad = XINPUT_GAMEPAD.read(segment.asSlice(GamepadOffset));
    return state;
  }

  /**
   * Writes the {@code XINPUT_STATE} to the given memory segment.
   *
   * @param segment The memory segment to write to.
   */
  void write(MemorySegment segment) {
    VH_dwPacketNumber.set(segment, 0, dwPacketNumber);
    Gamepad.write(segment.asSlice(GamepadOffset));
  }
}
