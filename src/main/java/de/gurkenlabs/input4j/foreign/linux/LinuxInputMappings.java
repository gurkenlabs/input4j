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
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LinuxInputMappings {

  private static final Logger log = Logger.getLogger(LinuxInputMappings.class.getName());

  private static final Map<MappingKey, ButtonMapping> BUTTON_MAPPINGS = new ConcurrentHashMap<>();
  private static final Map<MappingKey, AxisMapping> AXIS_MAPPINGS = new ConcurrentHashMap<>();

  private LinuxInputMappings() {}

  public static void registerButtonMapping(int vendorId, int productId,
      String deviceNamePattern, int linuxEventCode, InputComponent.ID buttonId) {
    MappingKey key = new MappingKey(vendorId, productId, deviceNamePattern, linuxEventCode);
    ButtonMapping mapping = new ButtonMapping(buttonId);
    BUTTON_MAPPINGS.put(key, mapping);
    log.log(Level.FINE, "Registered button mapping: VID={0}, PID={1}, pattern={2}, code={3} -> {4}",
        new Object[] {vendorId, productId, deviceNamePattern, linuxEventCode, buttonId});
  }

  public static void registerAxisMapping(int vendorId, int productId,
      String deviceNamePattern, int linuxEventCode, InputComponent.ID axisId) {
    MappingKey key = new MappingKey(vendorId, productId, deviceNamePattern, linuxEventCode);
    AxisMapping mapping = new AxisMapping(axisId);
    AXIS_MAPPINGS.put(key, mapping);
    log.log(Level.FINE, "Registered axis mapping: VID={0}, PID={1}, pattern={2}, code={3} -> {4}",
        new Object[] {vendorId, productId, deviceNamePattern, linuxEventCode, axisId});
  }

  public static void registerButtonMappingForType(ControllerType controllerType,
      int linuxEventCode, InputComponent.ID buttonId) {
    registerButtonMapping(-1, -1, "%" + controllerType.name() + "%", linuxEventCode, buttonId);
  }

  public static Optional<InputComponent.ID> getButtonMapping(int vendorId,
      int productId, String deviceName, int linuxEventCode) {
    for (Map.Entry<MappingKey, ButtonMapping> entry : BUTTON_MAPPINGS.entrySet()) {
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

      if (matchesDeviceName(deviceName, k.deviceNamePattern)) {
        return Optional.of(entry.getValue().buttonId);
      }
    }

    return Optional.empty();
  }

  public static Optional<InputComponent.ID> getAxisMapping(int vendorId,
      int productId, String deviceName, int linuxEventCode) {
    for (Map.Entry<MappingKey, AxisMapping> entry : AXIS_MAPPINGS.entrySet()) {
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

      if (matchesDeviceName(deviceName, k.deviceNamePattern)) {
        return Optional.of(entry.getValue().axisId);
      }
    }

    return Optional.empty();
  }

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
      if (matchesDeviceName(deviceName, k.deviceNamePattern)) {
        results.add(new MappingInfo(k.linuxEventCode, entry.getValue().buttonId));
      }
    }
    return results;
  }

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
      if (matchesDeviceName(deviceName, k.deviceNamePattern)) {
        results.add(new MappingInfo(k.linuxEventCode, entry.getValue().axisId));
      }
    }
    return results;
  }

  public static void clearCustomMappings() {
    BUTTON_MAPPINGS.clear();
    AXIS_MAPPINGS.clear();
    loadBuiltInMappings();
  }

  public record MappingInfo(int linuxEventCode, InputComponent.ID inputId) {}

  private static boolean matchesDeviceName(String deviceName, String pattern) {
    if (deviceName == null || pattern == null) {
      return false;
    }

    if (pattern.endsWith("%")) {
      String prefix = pattern.substring(0, pattern.length() - 1);
      return deviceName.startsWith(prefix);
    }

    return deviceName.equals(pattern);
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

    registerButtonMapping(-1, -1, "Generic USB Joystick%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(-1, -1, "Generic USB Joystick%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    registerButtonMapping(-1, -1, "Generic USB Joystick%", LinuxEventCode.BTN_2, Button.BUTTON_2);
    registerButtonMapping(-1, -1, "Generic USB Joystick%", LinuxEventCode.BTN_3, Button.BUTTON_3);
    registerButtonMapping(-1, -1, "Generic USB Gamepad%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    registerButtonMapping(-1, -1, "Generic USB Gamepad%", LinuxEventCode.BTN_1, Button.BUTTON_1);
  }

  static {
    loadBuiltInMappings();
  }

  private record MappingKey(int vendorId, int productId, String deviceNamePattern, int linuxEventCode) {}

  private record ButtonMapping(InputComponent.ID buttonId) {}

  private record AxisMapping(InputComponent.ID axisId) {}
}