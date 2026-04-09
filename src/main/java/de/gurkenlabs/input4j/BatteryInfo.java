package de.gurkenlabs.input4j;

/**
 * Represents battery information for a game controller.
 *
 * @param type  The type of battery.
 * @param level The current charge level.
 * @param charging Whether the controller is currently charging.
 */
public record BatteryInfo(BatteryType type, BatteryLevel level, boolean charging) {

  /**
   * Creates a BatteryInfo with explicit percentage value.
   * The level will be derived from the percentage.
   *
   * @param type The type of battery.
   * @param charging Whether the controller is currently charging.
   * @param percentage The actual battery percentage (0-100), or -1 if unknown.
   */
  public static BatteryInfo fromPercentage(BatteryType type, boolean charging, int percentage) {
    if (percentage < 0 || percentage > 100) {
      return new BatteryInfo(type, BatteryLevel.UNKNOWN, charging);
    }

    BatteryLevel level;
    if (percentage >= 75) {
      level = BatteryLevel.FULL;
    } else if (percentage >= 50) {
      level = BatteryLevel.MEDIUM;
    } else if (percentage >= 25) {
      level = BatteryLevel.LOW;
    } else {
      level = BatteryLevel.EMPTY;
    }

    return new BatteryInfo(type, level, charging);
  }

  /**
   * Returns whether battery information is available.
   * This is true when we have a valid level (not UNKNOWN) and the device is connected.
   *
   * @return true if battery info is available, false if unknown or not applicable
   */
  public boolean isAvailable() {
    return level != BatteryLevel.UNKNOWN && type != BatteryType.DISCONNECTED;
  }

  /**
   * Returns the battery level as a percentage (0-100).
   * Returns -1 if the level is unknown.
   *
   * @return battery percentage or -1 if unknown
   */
  public int getPercentage() {
    if (type == BatteryType.WIRED || type == BatteryType.DISCONNECTED) {
      return -1;
    }
    return switch (level) {
      case EMPTY -> 0;
      case LOW -> 25;
      case MEDIUM -> 50;
      case FULL -> 100;
      case UNKNOWN -> -1;
    };
  }
}