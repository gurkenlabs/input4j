package de.gurkenlabs.input4j.foreign.windows.xinput;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * The {@code XINPUT_CAPABILITIES} class represents the capabilities of an XInput device.
 * It includes information about the type, subtype, flags, gamepad, and vibration settings of the device.
 */
final class XINPUT_CAPABILITIES {
  /**
   * The type of the XInput device.
   * Native type: {@code BYTE}
   * Potential values:
   * <ul>
   *   <li>{@code 0x00}: Device</li>
   *   <li>{@code 0x01}: Headset</li>
   * </ul>
   */
  byte Type;

  /**
   * The subtype of the XInput device.
   * Native type: {@code BYTE}
   * Potential values:
   * <ul>
   *   <li>{@code 0x01}: Gamepad</li>
   *   <li>{@code 0x02}: Wheel</li>
   *   <li>{@code 0x03}: Arcade Stick</li>
   *   <li>{@code 0x04}: Flight Stick</li>
   *   <li>{@code 0x05}: Dance Pad</li>
   *   <li>{@code 0x06}: Guitar</li>
   *   <li>{@code 0x07}: Guitar Alternate</li>
   *   <li>{@code 0x08}: Drum Kit</li>
   *   <li>{@code 0x0A}: Guitar Bass</li>
   *   <li>{@code 0x0B}: Arcade Pad</li>
   * </ul>
   */
  byte SubType;

  /**
   * The flags indicating the capabilities of the XInput device.
   * Native type: {@code WORD}
   * Potential values:
   * <ul>
   *   <li>{@code 0x0001}: Voice supported</li>
   * </ul>
   */
  short Flags;

  /**
   * The gamepad capabilities of the XInput device.
   */
  XINPUT_GAMEPAD Gamepad;

  /**
   * The vibration capabilities of the XInput device.
   */
  XINPUT_VIBRATION Vibration;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_BYTE.withName("Type"),
          ValueLayout.JAVA_BYTE.withName("SubType"),
          ValueLayout.JAVA_SHORT.withName("Flags"),
          XINPUT_GAMEPAD.$LAYOUT.withName("Gamepad"),
          XINPUT_VIBRATION.$LAYOUT.withName("Vibration")
  );

  private static final VarHandle VH_Type = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("Type"));
  private static final VarHandle VH_SubType = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("SubType"));
  private static final VarHandle VH_Flags = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("Flags"));

  private static final long GamepadOffset = $LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Gamepad"));
  private static final long VibrationOffset = $LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Vibration"));

  /**
   * Reads the {@code XINPUT_CAPABILITIES} from the given memory segment.
   *
   * @param segment The memory segment to read from.
   * @return The {@code XINPUT_CAPABILITIES} instance.
   */
  static XINPUT_CAPABILITIES read(MemorySegment segment) {
    var capabilities = new XINPUT_CAPABILITIES();
    capabilities.Type = (byte) VH_Type.get(segment, 0);
    capabilities.SubType = (byte) VH_SubType.get(segment, 0);
    capabilities.Flags = (short) VH_Flags.get(segment, 0);
    capabilities.Gamepad = XINPUT_GAMEPAD.read(segment.asSlice(GamepadOffset));
    capabilities.Vibration = XINPUT_VIBRATION.read(segment.asSlice(VibrationOffset));
    return capabilities;
  }

  /**
   * Writes the {@code XINPUT_CAPABILITIES} to the given memory segment.
   *
   * @param segment The memory segment to write to.
   */
  void write(MemorySegment segment) {
    VH_Type.set(segment, 0, Type);
    VH_SubType.set(segment, 0, SubType);
    VH_Flags.set(segment, 0, Flags);
    Gamepad.write(segment.asSlice(GamepadOffset));
    Vibration.write(segment.asSlice(VibrationOffset));
  }

  public String getTypeName() {
    switch (SubType) {
      case 0x01:
        return "Gamepad";
      case 0x02:
        return "Wheel";
      case 0x03:
        return "Arcade Stick";
      case 0x04:
        return "Flight Stick";
      case 0x05:
        return "Dance Pad";
      case 0x06:
        return "Guitar";
      case 0x07:
        return "Guitar Alternate";
      case 0x08:
        return "Drum Kit";
      case 0x0B:
        return "Guitar Bass";
      case 0x0C:
        return "Arcade Pad";
      default:
        return "Unknown";
    }
  }
}
