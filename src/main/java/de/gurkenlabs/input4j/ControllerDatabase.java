package de.gurkenlabs.input4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database of known game controllers with their vendor and product IDs.
 * <p>
 * This class provides lookup functionality to identify controllers by their USB
 * vendor and product IDs, returning a display name and controller type.
 * </p>
 * <p>
 * Users can register custom controllers at runtime using the {@link #register}
 * method. Built-in controllers are loaded automatically.
 * </p>
 *
 * @see ControllerType
 */
public final class ControllerDatabase {

  /** Known vendor ID for Microsoft. */
  public static final int VENDOR_MICROSOFT = 0x045E;

  /** Known vendor ID for Sony. */
  public static final int VENDOR_SONY = 0x054C;

  /** Known vendor ID for Nintendo. */
  public static final int VENDOR_NINTENDO = 0x057E;

  /** Known vendor ID for 8BitDo. */
  public static final int VENDOR_8BITDO = 0x2DC8;

  /** Known vendor ID for Logitech. */
  public static final int VENDOR_LOGITECH = 0x046D;

  /** Known vendor ID for Thrustmaster. */
  public static final int VENDOR_THURSTMASTER = 0x044F;

  /** Known vendor ID for Mad Catz. */
  public static final int VENDOR_MAD_CATZ = 0x0738;

  /** Known vendor ID for Razer. */
  public static final int VENDOR_RAZER = 0x1532;

  private static final Map<Integer, ControllerInfo> BUILT_IN = buildBuiltInDatabase();
  private static final Map<Integer, ControllerInfo> CUSTOM = new ConcurrentHashMap<>();

  private ControllerDatabase() {}

  /**
   * Record containing controller information.
   *
   * @param displayName the user-friendly display name
   * @param type        the controller type
   */
  public record ControllerInfo(String displayName, ControllerType type) {}

  /**
   * Looks up a controller by vendor and product ID.
   * <p>
   * Custom registrations take precedence over built-in entries.
   * </p>
   *
   * @param vendorId  the USB vendor ID
   * @param productId the USB product ID
   * @return an Optional containing the controller info if found
   */
  public static Optional<ControllerInfo> lookup(int vendorId, int productId) {
    if (vendorId == -1 || productId == -1) {
      return Optional.empty();
    }

    int key = key(vendorId, productId);
    var info = CUSTOM.get(key);
    if (info != null) {
      return Optional.of(info);
    }
    return Optional.ofNullable(BUILT_IN.get(key));
  }

  /**
   * Gets the controller type for a vendor/product pair.
   *
   * @param vendorId  the USB vendor ID
   * @param productId the USB product ID
   * @return the controller type, or GENERIC if not found
   */
  public static ControllerType getControllerType(int vendorId, int productId) {
    return lookup(vendorId, productId)
        .map(ControllerInfo::type)
        .orElse(ControllerType.fromVendorProduct(vendorId, productId));
  }

  /**
   * Gets the display name for a vendor/product pair.
   *
   * @param vendorId  the USB vendor ID
   * @param productId the USB product ID
   * @return the display name, or null if not found
   */
  public static String getDisplayName(int vendorId, int productId) {
    return lookup(vendorId, productId)
        .map(ControllerInfo::displayName)
        .orElse(null);
  }

  /**
   * Registers a custom controller.
   * <p>
   * This allows users to add support for controllers not in the built-in database.
   * Custom registrations take precedence over built-in entries with the same
   * vendor/product ID.
   * </p>
   *
   * @param vendorId    the USB vendor ID
   * @param productId  the USB product ID
   * @param displayName the user-friendly display name
   * @param type        the controller type
   */
  public static void register(int vendorId, int productId, String displayName, ControllerType type) {
    if (vendorId == -1 || productId == -1 || displayName == null || type == null) {
      throw new IllegalArgumentException("vendorId, productId, displayName, and type must not be null");
    }
    int key = key(vendorId, productId);
    CUSTOM.put(key, new ControllerInfo(displayName, type));
  }

  /**
   * Removes a custom controller registration.
   *
   * @param vendorId  the USB vendor ID
   * @param productId the USB product ID
   */
  public static void unregister(int vendorId, int productId) {
    int key = key(vendorId, productId);
    CUSTOM.remove(key);
  }

  /**
   * Clears all custom controller registrations.
   */
  public static void clearCustom() {
    CUSTOM.clear();
  }

  private static int key(int vendorId, int productId) {
    return (vendorId << 16) | (productId & 0xFFFF);
  }

  private static Map<Integer, ControllerInfo> buildBuiltInDatabase() {
    return Map.ofEntries(
        // Microsoft Xbox
        entry(0x045E, 0x028E, "Xbox 360 Controller", ControllerType.XBOX),
        entry(0x045E, 0x02DD, "Xbox One Controller", ControllerType.XBOX),
        entry(0x045E, 0x02E0, "Xbox One S Controller", ControllerType.XBOX),
        entry(0x045E, 0x02E3, "Xbox One Elite Controller", ControllerType.XBOX),
        entry(0x045E, 0x02EA, "Xbox One S Controller (USB)", ControllerType.XBOX),
        entry(0x045E, 0x02FF, "Xbox Wireless Controller", ControllerType.XBOX),
        entry(0x045E, 0x0B13, "Xbox Wireless Controller (Bluetooth)", ControllerType.XBOX),
        entry(0x045E, 0x0719, "Xbox 360 Wireless Receiver", ControllerType.XBOX),
        entry(0x045E, 0x0291, "Xbox 360 Wireless Controller (Unlicensed)", ControllerType.XBOX),

        // Sony PlayStation
        entry(0x054C, 0x0268, "DualShock 3", ControllerType.PLAYSTATION),
        entry(0x054C, 0x05C4, "DualShock 4", ControllerType.PLAYSTATION),
        entry(0x054C, 0x09CC, "DualShock 4 (CUH-ZCT2)", ControllerType.PLAYSTATION),
        entry(0x054C, 0x0BA0, "DualShock 4 USB Receiver", ControllerType.PLAYSTATION),
        entry(0x054C, 0x0CE6, "DualSense", ControllerType.PLAYSTATION),
        entry(0x054C, 0x0DF2, "DualSense Edge", ControllerType.PLAYSTATION),

        // Nintendo
        entry(0x057E, 0x0306, "Wii Remote", ControllerType.NINTENDO),
        entry(0x057E, 0x0330, "Wii U Pro Controller", ControllerType.NINTENDO),
        entry(0x057E, 0x0337, "GameCube Controller Adapter", ControllerType.NINTENDO),
        entry(0x057E, 0x2006, "Joy-Con (L)", ControllerType.NINTENDO),
        entry(0x057E, 0x2007, "Joy-Con (R)", ControllerType.NINTENDO),
        entry(0x057E, 0x2009, "Nintendo Switch Pro Controller", ControllerType.NINTENDO),
        entry(0x057E, 0x200E, "Joy-Con Charging Grip", ControllerType.NINTENDO),

        // 8BitDo
        entry(0x2DC8, 0x1003, "8BitDo N30 Arcade", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x1080, "8BitDo N30 Arcade", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x2810, "8BitDo F30", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x2820, "8BitDo N30", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x2830, "8BitDo SF30", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x2840, "8BitDo SN30", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x3000, "8BitDo SN30", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x3001, "8BitDo SF30", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x3810, "8BitDo F30 Pro", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x3820, "8BitDo N30 Pro", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x3830, "8BitDo RB864", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x6000, "8BitDo SF30 Pro", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x6001, "8BitDo SN30 Pro+", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x6100, "8BitDo SF30 Pro", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x6101, "8BitDo SN30 Pro+", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x9000, "8BitDo F30 Pro", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x9001, "8BitDo NES30 Pro", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x9002, "8BitDo RB864", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x3013, "8BitDo Ultimate (Bluetooth)", ControllerType.EIGHTBITDO),
        entry(0x2DC8, 0x3106, "8BitDo Ultimate (2.4GHz)", ControllerType.EIGHTBITDO),
        entry(0x05A0, 0x3232, "8BitDo Zero", ControllerType.EIGHTBITDO),

        // Logitech
        entry(0x046D, 0xC218, "Logitech F510", ControllerType.GENERIC),
        entry(0x046D, 0xC219, "Logitech F710", ControllerType.GENERIC),
        entry(0x046D, 0xC21D, "Logitech F310 (XInput)", ControllerType.GENERIC),
        entry(0x046D, 0xC21F, "Logitech F710 (XInput)", ControllerType.GENERIC),
        entry(0x046D, 0xC242, "Logitech ChillStream", ControllerType.GENERIC),
        entry(0x046D, 0xC261, "Logitech G920", ControllerType.GENERIC),

        // Thrustmaster
        entry(0x044F, 0x0F00, "Thrustmaster Wheel Xbox", ControllerType.GENERIC),
        entry(0x044F, 0x0F03, "Thrustmaster Wheel Xbox", ControllerType.GENERIC),
        entry(0x044F, 0xB671, "Thrustmaster Ferrari 458 Spider", ControllerType.GENERIC),
        entry(0x044F, 0xD001, "Thrustmaster T Mini", ControllerType.GENERIC),

        // Mad Catz
        entry(0x0738, 0x4716, "Mad Catz Xbox 360", ControllerType.GENERIC),
        entry(0x0738, 0x4736, "Mad Catz MicroCon Xbox 360", ControllerType.GENERIC),
        entry(0x0738, 0x4740, "Mad Catz BeatPad Xbox 360", ControllerType.GENERIC),
        entry(0x0738, 0x8250, "Mad Catz FightPad Pro PS4", ControllerType.GENERIC),

        // Razer
        entry(0x1532, 0x0900, "Razer Serval", ControllerType.GENERIC),
        entry(0x1532, 0x0A00, "Razer Atrox", ControllerType.GENERIC),
        entry(0x1532, 0x0A14, "Razer Wolverine Ultimate", ControllerType.GENERIC),
        entry(0x1532, 0x1000, "Razer Raiju", ControllerType.GENERIC),

        // Other popular controllers
        entry(0x0F0D, 0x00C5, "Hori Fighting Commander", ControllerType.GENERIC),
        entry(0x0F0D, 0x0084, "Hori Fighting Commander 5", ControllerType.GENERIC),
        entry(0x0955, 0x7210, "NVIDIA Shield Controller", ControllerType.GENERIC),
        entry(0x1949, 0x0402, "Ipega PG-9023", ControllerType.GENERIC),
        entry(0x24C6, 0x541A, "PowerA Mini Xbox One", ControllerType.GENERIC),
        entry(0x20D6, 0x6271, "Moga Pro 2", ControllerType.GENERIC)
    );
  }

  private static Map.Entry<Integer, ControllerInfo> entry(int vendorId, int productId, String name, ControllerType type) {
    return Map.entry(key(vendorId, productId), new ControllerInfo(name, type));
  }
}