package de.gurkenlabs.input4j.foreign.windows.xinput;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * Represents battery information for an XInput device.
 */
final class XINPUT_BATTERY_INFORMATION {
  static final int BATTERY_TYPE_DISCONNECTED = 0;
  static final int BATTERY_TYPE_WIRED = 1;
  static final int BATTERY_TYPE_ALKALINE = 2;
  static final int BATTERY_TYPE_NIMH = 3;
  static final int BATTERY_TYPE_UNKNOWN = (byte) 0xFF;

  static final int BATTERY_LEVEL_EMPTY = 0;
  static final int BATTERY_LEVEL_LOW = 1;
  static final int BATTERY_LEVEL_MEDIUM = 2;
  static final int BATTERY_LEVEL_FULL = 3;
  static final int BATTERY_LEVEL_UNPLUGGED = 0;

  static final int BATTERY_DEVTYPE_GAMEPAD = 0;
  static final int BATTERY_DEVTYPE_HEADSET = 1;

  byte BatteryType;
  byte BatteryLevel;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
      ValueLayout.JAVA_BYTE.withName("BatteryType"),
      ValueLayout.JAVA_BYTE.withName("BatteryLevel")
  );

  private static final VarHandle VH_BatteryType = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("BatteryType"));
  private static final VarHandle VH_BatteryLevel = $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("BatteryLevel"));

  static XINPUT_BATTERY_INFORMATION read(MemorySegment segment) {
    var batteryInfo = new XINPUT_BATTERY_INFORMATION();
    batteryInfo.BatteryType = (byte) VH_BatteryType.get(segment, 0);
    batteryInfo.BatteryLevel = (byte) VH_BatteryLevel.get(segment, 0);
    return batteryInfo;
  }

  void write(MemorySegment segment) {
    VH_BatteryType.set(segment, 0, BatteryType);
    VH_BatteryLevel.set(segment, 0, BatteryLevel);
  }
}