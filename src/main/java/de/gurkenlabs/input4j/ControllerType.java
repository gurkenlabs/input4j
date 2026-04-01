package de.gurkenlabs.input4j;

import java.util.Locale;

/**
 * Represents the type of game controller.
 * This enum is used to identify the category of a controller based on its vendor and product IDs.
 */
public enum ControllerType {
  /** Microsoft Xbox controllers. */
  XBOX,

  /** Sony PlayStation controllers (DualShock, DualSense). */
  PLAYSTATION,

  /** Nintendo controllers (Switch Pro, Joy-Cons). */
  NINTENDO,

  /** 8BitDo retro controllers. */
  EIGHTBITDO,

  /** Generic or unknown controller type. */
  GENERIC;

  /**
   * Returns the display name of this controller type.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return switch (this) {
      case XBOX -> "Xbox Controller";
      case PLAYSTATION -> "PlayStation Controller";
      case NINTENDO -> "Nintendo Controller";
      case EIGHTBITDO -> "8BitDo Controller";
      case GENERIC -> "Game Controller";
    };
  }

  /**
   * Returns the controller type from vendor and product IDs.
   *
   * @param vendorId  the USB vendor ID
   * @param productId the USB product ID
   * @return the controller type
   */
  public static ControllerType fromVendorProduct(int vendorId, int productId) {
    if (vendorId == -1 || productId == -1) {
      return GENERIC;
    }

    return switch (vendorId) {
      case 0x045E -> XBOX; // Microsoft
      case 0x054C -> PLAYSTATION; // Sony
      case 0x057E -> NINTENDO; // Nintendo
      case 0x2DC8 -> EIGHTBITDO; // 8BitDo
      default -> GENERIC;
    };
  }
}