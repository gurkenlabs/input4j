package de.gurkenlabs.input4j.foreign.macos.iokit;

/**
 * Defines rumble report profiles for different game controllers.
 * Each profile specifies the report ID, report size, and byte positions for motors.
 *
 * <p>Known controller configurations:</p>
 * <ul>
 *   <li>Xbox One/Series X: Report ID 0x09, 6 bytes</li>
 *   <li>DualShock 4: Report ID 0x05, 31 bytes</li>
 *   <li>DualSense: Report ID 0x02, 47 bytes</li>
 *   <li>Nintendo Pro Controller: Report ID 0x10, 10 bytes</li>
 *   <li>8BitDo Controllers: Various formats (see specific models)</li>
 * </ul>
 *
 * @see CoreFoundation Framework (Apple Developer Documentation)
 * @see IOHID Manager (Apple Developer Documentation)
 * @see USB HID Specification (USB-IF)  
 */
public enum ControllerRumbleProfile {
  /** Generic gamepad using standard 3-byte format (Report ID 0x00) */
  GENERIC((byte) 0x00, 3, 1, 2),

  /** Microsoft Xbox One Controller (USB) */
  XBOX_ONE((byte) 0x09, 6, 1, 2),

  /** Microsoft Xbox Series X Controller (USB) */
  XBOX_SERIES_X((byte) 0x09, 6, 1, 2),

  /** Sony DualShock 4 Controller */
  DUALSHOCK_4((byte) 0x05, 31, 4, 5),

  /** Sony DualSense Controller */
  DUALSENSE((byte) 0x02, 47, 4, 5),

  /** Nintendo Pro Controller */
  NINTENDO_PRO_CONTROLLER((byte) 0x10, 10, 5, 9),

  /** 8BitDo Controllers (Generic - uses standard HID gamepad format) */
  EIGHTBITDO_GENERIC((byte) 0x00, 3, 1, 2),

  /** 8BitDo SN30 Pro+ (uses DualShock 4-like format) */
  EIGHTBITDO_SN30_PRO_PLUS((byte) 0x05, 31, 4, 5);

  private final byte reportId;
  private final int reportSize;
  private final int leftMotorOffset;
  private final int rightMotorOffset;

  ControllerRumbleProfile(byte reportId, int reportSize, int leftMotorOffset, int rightMotorOffset) {
    this.reportId = reportId;
    this.reportSize = reportSize;
    this.leftMotorOffset = leftMotorOffset;
    this.rightMotorOffset = rightMotorOffset;
  }

  public byte getReportId() {
    return reportId;
  }

  public int getReportSize() {
    return reportSize;
  }

  public int getLeftMotorOffset() {
    return leftMotorOffset;
  }

  public int getRightMotorOffset() {
    return rightMotorOffset;
  }

  /**
   * Looks up a controller profile by vendor and product ID.
   *
   * @param vendorId  The vendor ID (e.g., 0x045E for Microsoft).
   * @param productId The product ID.
   * @return The controller profile, or GENERIC if not recognized.
   */
  public static ControllerRumbleProfile fromVendorProduct(int vendorId, int productId) {
    return switch (vendorId) {
      case 0x045E -> { // Microsoft
        yield switch (productId) {
          case 0x02DD, 0x02E0 -> XBOX_ONE; // Xbox One
          case 0x0B00, 0x0B05 -> XBOX_SERIES_X; // Xbox Series X
          default -> GENERIC;
        };
      }
      case 0x054C -> { // Sony
        yield switch (productId) {
          case 0x09CC -> DUALSHOCK_4;
          case 0x0CE6 -> DUALSENSE;
          default -> GENERIC;
        };
      }
      case 0x057E -> { // Nintendo
        yield switch (productId) {
          case 0x2009 -> NINTENDO_PRO_CONTROLLER;
          default -> GENERIC;
        };
      }
      case 0x2DC8 -> { // 8BitDo
        yield switch (productId) {
          case 0x6002 -> EIGHTBITDO_SN30_PRO_PLUS; // SN30 Pro+
          default -> EIGHTBITDO_GENERIC; // Most 8BitDo controllers use standard HID
        };
      }
      default -> GENERIC;
    };
  }

  /**
   * Creates a default profile for unknown devices.
   *
   * @return The GENERIC profile.
   */
  public static ControllerRumbleProfile defaultProfile() {
    return GENERIC;
  }
}
