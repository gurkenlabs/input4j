package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.ControllerType;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.components.Axis;
import de.gurkenlabs.input4j.components.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for Linux evdev button and axis mappings.
 *
 * <p>Maps Linux input event codes to standardized {@link de.gurkenlabs.input4j.InputComponent.ID}
 * values based on vendor ID, product ID, and device name patterns. Supports both specific device
 * mappings and generic fallbacks using prefix patterns (e.g., {@code "GameSir%"}).
 *
 * <p>Built-in mappings are loaded automatically on class initialization. Custom mappings can be
 * registered via {@link #registerButtonMapping} and {@link #registerAxisMapping}.
 */
public final class LinuxInputMappings {

  private static final Logger log = Logger.getLogger(LinuxInputMappings.class.getName());

  private static final Map<MappingKey, ButtonMapping> BUTTON_MAPPINGS = new ConcurrentHashMap<>();
  private static final Map<MappingKey, AxisMapping> AXIS_MAPPINGS = new ConcurrentHashMap<>();

  private LinuxInputMappings() {}

  /**
   * Registers a button mapping for a specific device.
   *
   * @param vendorId the USB vendor ID, or -1 to match any vendor
   * @param productId the USB product ID, or -1 to match any product
   * @param deviceNamePattern a device name pattern; if it ends with {@code %}, matches by prefix
   * @param linuxEventCode the Linux input event code (e.g., {@code BTN_0})
   * @param buttonId the standardized button ID to map to
   */
  public static void registerButtonMapping(int vendorId, int productId,
      String deviceNamePattern, int linuxEventCode, InputComponent.ID buttonId) {
    MappingKey key = new MappingKey(vendorId, productId, deviceNamePattern, linuxEventCode);
    ButtonMapping mapping = new ButtonMapping(buttonId);
    BUTTON_MAPPINGS.put(key, mapping);
    log.log(Level.FINE, "Registered button mapping: VID={0}, PID={1}, pattern={2}, code={3} -> {4}",
        new Object[] {vendorId, productId, deviceNamePattern, linuxEventCode, buttonId});
  }

  /**
   * Registers an axis mapping for a specific device.
   *
   * @param vendorId the USB vendor ID, or -1 to match any vendor
   * @param productId the USB product ID, or -1 to match any product
   * @param deviceNamePattern a device name pattern; if it ends with {@code %}, matches by prefix
   * @param linuxEventCode the Linux input event code (e.g., {@code ABS_X})
   * @param axisId the standardized axis ID to map to
   */
  public static void registerAxisMapping(int vendorId, int productId,
      String deviceNamePattern, int linuxEventCode, InputComponent.ID axisId) {
    MappingKey key = new MappingKey(vendorId, productId, deviceNamePattern, linuxEventCode);
    AxisMapping mapping = new AxisMapping(axisId);
    AXIS_MAPPINGS.put(key, mapping);
    log.log(Level.FINE, "Registered axis mapping: VID={0}, PID={1}, pattern={2}, code={3} -> {4}",
        new Object[] {vendorId, productId, deviceNamePattern, linuxEventCode, axisId});
  }

  /**
   * Registers a button mapping for a controller type (e.g., XBOX, PLAYSTATION).
   * Uses a device name pattern based on the controller type name.
   *
   * @param controllerType the controller type
   * @param linuxEventCode the Linux input event code
   * @param buttonId the standardized button ID to map to
   */
  public static void registerButtonMappingForType(ControllerType controllerType,
      int linuxEventCode, InputComponent.ID buttonId) {
    registerButtonMapping(-1, -1, "%" + controllerType.name() + "%", linuxEventCode, buttonId);
  }

  /**
   * Looks up a button mapping for the given device and event code.
   * When multiple mappings match, the most specific one is selected based on
   * specificity scoring: exact VID/PID match ranks higher than wildcards,
   * and exact device name match ranks higher than prefix patterns.
   * If multiple mappings have the same score, the result is non-deterministic.
   *
   * @param vendorId the USB vendor ID
   * @param productId the USB product ID
   * @param deviceName the device name
   * @param linuxEventCode the Linux input event code
   * @return the mapped button ID, or empty if no mapping matches
   */
  public static Optional<InputComponent.ID> getButtonMapping(int vendorId,
      int productId, String deviceName, int linuxEventCode) {
    return findBestMatch(BUTTON_MAPPINGS, vendorId, productId, deviceName, linuxEventCode,
        entry -> entry.getValue().buttonId);
  }

  /**
   * Looks up an axis mapping for the given device and event code.
   * When multiple mappings match, the most specific one is selected based on
   * specificity scoring: exact VID/PID match ranks higher than wildcards,
   * and exact device name match ranks higher than prefix patterns.
   * If multiple mappings have the same score, the result is non-deterministic.
   *
   * @param vendorId the USB vendor ID
   * @param productId the USB product ID
   * @param deviceName the device name
   * @param linuxEventCode the Linux input event code
   * @return the mapped axis ID, or empty if no mapping matches
   */
  public static Optional<InputComponent.ID> getAxisMapping(int vendorId,
      int productId, String deviceName, int linuxEventCode) {
    return findBestMatch(AXIS_MAPPINGS, vendorId, productId, deviceName, linuxEventCode,
        entry -> entry.getValue().axisId);
  }

  private static <T, R> Optional<R> findBestMatch(Map<MappingKey, T> map, int vendorId,
      int productId, String deviceName, int linuxEventCode,
      Function<Map.Entry<MappingKey, T>, R> extractor) {
    R bestResult = null;
    int bestScore = -1;

    for (Map.Entry<MappingKey, T> entry : map.entrySet()) {
      MappingKey k = entry.getKey();

      if (k.linuxEventCode != linuxEventCode) {
        continue;
      }

      if (k.vendorId != -1 && k.vendorId != vendorId) {
        continue;
      }

      if (k.productId != -1 && k.productId != productId) {
        continue;
      }

      int nameScore = scoreDeviceName(deviceName, k.deviceNamePattern);
      if (nameScore == 0) {
        continue;
      }

      int score = scoreSpecificity(k.vendorId, k.productId) + nameScore;
      if (score > bestScore) {
        bestScore = score;
        bestResult = extractor.apply(entry);
      }
    }

    return bestResult != null ? Optional.of(bestResult) : Optional.empty();
  }

  private static int scoreSpecificity(int vendorId, int productId) {
    int score = 0;
    if (vendorId != -1) {
      score += 2;
    }
    if (productId != -1) {
      score += 2;
    }
    return score;
  }

  private static int scoreDeviceName(String deviceName, String pattern) {
    if (deviceName == null || pattern == null) {
      return 0;
    }

    if (pattern.endsWith("%")) {
      String prefix = pattern.substring(0, pattern.length() - 1);
      if (deviceName.startsWith(prefix)) {
        return 1;
      }
      return 0;
    }

    if (deviceName.equals(pattern)) {
      return 3;
    }
    return 0;
  }

  /**
   * Returns all button mappings applicable to the given device.
   *
   * @param vendorId the USB vendor ID
   * @param productId the USB product ID
   * @param deviceName the device name
   * @return a list of matching button mappings
   */
  public static List<MappingInfo> getButtonMappingsForDevice(int vendorId,
      int productId, String deviceName) {
    List<MappingInfo> results = new ArrayList<>();
    for (Map.Entry<MappingKey, ButtonMapping> entry : BUTTON_MAPPINGS.entrySet()) {
      MappingKey k = entry.getKey();
      if (k.vendorId != -1 && k.vendorId != vendorId) {
        continue;
      }
      if (k.productId != -1 && k.productId != productId) {
        continue;
      }
      if (scoreDeviceName(deviceName, k.deviceNamePattern) > 0) {
        results.add(new MappingInfo(k.linuxEventCode, entry.getValue().buttonId));
      }
    }
    return results;
  }

  /**
   * Returns all axis mappings applicable to the given device.
   *
   * @param vendorId the USB vendor ID
   * @param productId the USB product ID
   * @param deviceName the device name
   * @return a list of matching axis mappings
   */
  public static List<MappingInfo> getAxisMappingsForDevice(int vendorId,
      int productId, String deviceName) {
    List<MappingInfo> results = new ArrayList<>();
    for (Map.Entry<MappingKey, AxisMapping> entry : AXIS_MAPPINGS.entrySet()) {
      MappingKey k = entry.getKey();
      if (k.vendorId != -1 && k.vendorId != vendorId) {
        continue;
      }
      if (k.productId != -1 && k.productId != productId) {
        continue;
      }
      if (scoreDeviceName(deviceName, k.deviceNamePattern) > 0) {
        results.add(new MappingInfo(k.linuxEventCode, entry.getValue().axisId));
      }
    }
    return results;
  }

  /**
   * Clears all custom mappings and reloads the built-in mappings.
   */
  public static void resetMappings() {
    BUTTON_MAPPINGS.clear();
    AXIS_MAPPINGS.clear();
    loadBuiltInMappings();
  }

  /**
   * Information about a single mapping entry.
   *
   * @param linuxEventCode the Linux input event code
   * @param inputId the standardized input component ID
   */
  public record MappingInfo(int linuxEventCode, InputComponent.ID inputId) {
  }

  static void loadBuiltInMappings() {
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_2, Button.BUTTON_2);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_3, Button.BUTTON_3);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_4, Button.BUTTON_4);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_5, Button.BUTTON_5);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_6, Button.BUTTON_6);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_7, Button.BUTTON_7);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_8, Button.BUTTON_8);
    registerButtonMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.BTN_9, Button.BUTTON_9);

    registerButtonMapping(0x05AC, 0x055B, "GameSir G3w", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x05AC, 0x055B, "GameSir G3w", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x05AC, 0x024D, "GameSir G4", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x05AC, 0x024D, "GameSir G4", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x05AC, 0x044D, "GameSir G4", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x05AC, 0x044D, "GameSir G4", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x05AC, 0x02D, "GameSir G4s", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x05AC, 0x02D, "GameSir G4s", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x05AC, 0x057A, "GameSir G5", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x05AC, 0x057A, "GameSir G5", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x05AC, 0x061A, "GameSir T3", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x05AC, 0x061A, "GameSir T3", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x3735, 0x0004, "GameSir T4%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x3735, 0x0004, "GameSir T4%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x3735, 0x0011, "GameSir X4A", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x3735, 0x0011, "GameSir X4A", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x3735, 0x000B, "GameSir Cyclone 2", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x3735, 0x000B, "GameSir Cyclone 2", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x3735, 0x0097, "GameSir Kaleid Flux", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x3735, 0x0097, "GameSir Kaleid Flux", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x3735, 0x0094, "GameSir Tegenaria Lite", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x3735, 0x0094, "GameSir Tegenaria Lite", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x3735, 0x0022, "GameSir G7 Pro", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x3735, 0x0022, "GameSir G7 Pro", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x5585, 0x061B, "GameSir G4 Pro", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x5585, 0x061B, "GameSir G4 Pro", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0xBC20, 0x5656, "GameSir T4w", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0xBC20, 0x5656, "GameSir T4w", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(-1, -1, "Generic USB Joystick%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(-1, -1, "Generic USB Joystick%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(-1, -1, "Generic USB Joystick%", LinuxEventCode.BTN_2, Button.BUTTON_2);
    registerButtonMapping(-1, -1, "Generic USB Joystick%", LinuxEventCode.BTN_3, Button.BUTTON_3);

    registerButtonMapping(0x0079, -1, "DragonRise%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x0079, -1, "DragonRise%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x0079, -1, "DragonRise%", LinuxEventCode.BTN_2, Button.BUTTON_2);
    registerButtonMapping(0x0079, -1, "DragonRise%", LinuxEventCode.BTN_3, Button.BUTTON_3);
    registerButtonMapping(0x0079, -1, "DragonRise%", LinuxEventCode.BTN_4, Button.BUTTON_4);
    registerButtonMapping(0x0079, -1, "DragonRise%", LinuxEventCode.BTN_5, Button.BUTTON_5);

    registerButtonMapping(0x2DC8, 0x0130, "8BitDo Ultimate%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x2DC8, 0x0130, "8BitDo Ultimate%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x0D2E, 0x0209, "Anbernic%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x0D2E, 0x0209, "Anbernic%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x3735, 0x0010, "Anbernic%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x3735, 0x0010, "Anbernic%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x3735, 0x0046, "Anbernic%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x3735, 0x0046, "Anbernic%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x1949, 0x0402, "Fire%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x1949, 0x0402, "Fire%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x1949, 0x0402, "Fire%", LinuxEventCode.BTN_2, Button.BUTTON_2);
    registerButtonMapping(0x1949, 0x0402, "Fire%", LinuxEventCode.BTN_3, Button.BUTTON_3);

    registerButtonMapping(0x0955, 0x7210, "NVIDIA Shield%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x0955, 0x7210, "NVIDIA Shield%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x1949, 0x0402, "iPega%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x1949, 0x0402, "iPega%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x20D6, -1, "Moga%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x20D6, -1, "Moga%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x24C6, -1, "PowerA%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x24C6, -1, "PowerA%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x1242, -1, "EasySMX%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x1242, -1, "EasySMX%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x1242, -1, "EasySMX%", LinuxEventCode.BTN_2, Button.BUTTON_2);
    registerButtonMapping(0x1242, -1, "EasySMX%", LinuxEventCode.BTN_3, Button.BUTTON_3);

    registerButtonMapping(-1, -1, "Retro%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(-1, -1, "Retro%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(-1, -1, "Retro%", LinuxEventCode.BTN_2, Button.BUTTON_2);
    registerButtonMapping(-1, -1, "Retro%", LinuxEventCode.BTN_3, Button.BUTTON_3);

    registerButtonMapping(-1, -1, "USB Gamepad%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(-1, -1, "USB Gamepad%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(-1, -1, "USB Gamepad%", LinuxEventCode.BTN_2, Button.BUTTON_2);
    registerButtonMapping(-1, -1, "USB Gamepad%", LinuxEventCode.BTN_3, Button.BUTTON_3);

    registerButtonMapping(0x2DC8, 0x1251, "8BitDo Lite 2%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x2DC8, 0x1251, "8BitDo Lite 2%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x2DC8, 0x1890, "8BitDo Zero 2%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x2DC8, 0x1890, "8BitDo Zero 2%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x2DC8, 0x1151, "8BitDo Lite SE%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x2DC8, 0x1151, "8BitDo Lite SE%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x1532, 0x1000, "Razer%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x1532, 0x1000, "Razer%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(-1, -1, "Snakebyte%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(-1, -1, "Snakebyte%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x0F0D, -1, "Hori%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x0F0D, -1, "Hori%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x044F, -1, "Thrustmaster%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x044F, -1, "Thrustmaster%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x046D, 0xC21D, "Logitech F310%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x046D, 0xC21D, "Logitech F310%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x046D, 0xC218, "Logitech F510%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x046D, 0xC218, "Logitech F510%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerButtonMapping(0x054C, 0x05C4, "DualShock 4%", LinuxEventCode.BTN_0, Button.BUTTON_1);
    registerButtonMapping(0x054C, 0x05C4, "DualShock 4%", LinuxEventCode.BTN_1, Button.BUTTON_2);
    registerButtonMapping(0x054C, 0x0CE6, "DualSense%", LinuxEventCode.BTN_0, Button.BUTTON_1);
    registerButtonMapping(0x054C, 0x0CE6, "DualSense%", LinuxEventCode.BTN_1, Button.BUTTON_2);

    registerButtonMapping(0x057E, 0x2006, "Joy-Con%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x057E, 0x2006, "Joy-Con%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x057E, 0x2007, "Joy-Con%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x057E, 0x2007, "Joy-Con%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(0x057E, 0x2009, "Pro Controller%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(0x057E, 0x2009, "Pro Controller%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    registerAxisMapping(-1, -1, "Generic%", LinuxEventCode.ABS_Z, Axis.AXIS_Z);
    registerAxisMapping(-1, -1, "Generic%", LinuxEventCode.ABS_RZ, Axis.AXIS_RZ);

    registerAxisMapping(0x05AC, 0x03DD, "GameSir G3%", LinuxEventCode.ABS_Z, Axis.AXIS_Z);
    registerAxisMapping(0x05AC, 0x03DD, "GameSir G3%", LinuxEventCode.ABS_RZ, Axis.AXIS_RZ);

    registerAxisMapping(0x2DC8, -1, "8BitDo%", LinuxEventCode.ABS_Z, Axis.AXIS_Z);
    registerAxisMapping(0x2DC8, -1, "8BitDo%", LinuxEventCode.ABS_RZ, Axis.AXIS_RZ);

    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_SOUTH, Button.BUTTON_0);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_EAST, Button.BUTTON_1);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_NORTH, Button.BUTTON_2);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_WEST, Button.BUTTON_3);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_TL, Button.BUTTON_4);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_TR, Button.BUTTON_5);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_SELECT, Button.BUTTON_6);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_START, Button.BUTTON_7);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_THUMBL, Button.BUTTON_8);
    registerButtonMappingForType(ControllerType.XBOX, LinuxEventCode.BTN_THUMBR, Button.BUTTON_9);

    registerButtonMappingForType(ControllerType.PLAYSTATION, LinuxEventCode.BTN_0, Button.BUTTON_1);
    registerButtonMappingForType(ControllerType.PLAYSTATION, LinuxEventCode.BTN_1, Button.BUTTON_2);
    registerButtonMappingForType(ControllerType.PLAYSTATION, LinuxEventCode.BTN_2, Button.BUTTON_3);
    registerButtonMappingForType(ControllerType.PLAYSTATION, LinuxEventCode.BTN_3, Button.BUTTON_0);

    registerButtonMapping(-1, -1, "Generic USB Gamepad%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(-1, -1, "Generic USB Gamepad%", LinuxEventCode.BTN_1, Button.BUTTON_1);
  }

  static {
    loadBuiltInMappings();
  }

  private record MappingKey(int vendorId, int productId, String deviceNamePattern, int linuxEventCode) {
  }

  private record ButtonMapping(InputComponent.ID buttonId) {
  }

  private record AxisMapping(InputComponent.ID axisId) {
  }
}
