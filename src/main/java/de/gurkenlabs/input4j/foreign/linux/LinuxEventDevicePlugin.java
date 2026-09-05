package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.BatteryInfo;
import de.gurkenlabs.input4j.BatteryLevel;
import de.gurkenlabs.input4j.BatteryType;
import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.components.Axis;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@code LinuxEventDevicePlugin} class is responsible for managing Linux event devices.
 * It initializes and adds them to the collection of devices.
 * <p>
 * The joystick API (/dev/input/jsX) is considered legacy and is no longer actively developed.
 * The evdev API (/dev/input/eventX) has largely replaced it because it is more flexible and supports additional features like force feedback.
 * Reasons to use evdev over the joystick API:
 * <ul>
 *   <li>evdev is the modern Linux input API and is actively developed.</li>
 *   <li>evdev is more flexible and supports additional features like force feedback.</li>
 *   <li>evdev is the preferred API for newer software and libraries like SDL, libevdev, and udev.</li>
 * </ul>
 */
public class LinuxEventDevicePlugin extends AbstractInputDevicePlugin {
  private final Map<String, LinuxEventDevice> nativeDevices = new ConcurrentHashMap<>();

  @Override
  public void internalInitDevices(Frame owner) {
    this.setDevices(refreshInputDevices());
  }

  @Override
  public void close() {
    super.close();
    for (LinuxEventDevice device : nativeDevices.values()) {
      device.close();
    }

    this.nativeDevices.clear();
  }

  @Override
  protected Collection<InputDevice> refreshInputDevices() {
    final File dev = new File("/dev/input");
    File[] eventDeviceFiles = dev.listFiles((File _, String name) -> name.startsWith("event"));
    if (eventDeviceFiles == null) {
      eventDeviceFiles = new File[0];
    } else {
      Arrays.sort(eventDeviceFiles, Comparator.comparing(File::getName));
    }

    var currentPaths =
        Arrays.stream(eventDeviceFiles)
            .map(File::getAbsolutePath)
            .collect(Collectors.toSet());

    // Check existing native devices: remove any that are no longer present or disconnected
    for (var entry : this.nativeDevices.entrySet()) {
      var deviceId = entry.getKey();
      var device = entry.getValue();

      boolean disconnected =
          device.isDisconnected()
              || !currentPaths.contains(device.filename)
              || !new File(device.filename).exists();

      if (disconnected) {
        device.close();
        this.nativeDevices.remove(deviceId);
      }
    }

    var refreshedDevices = new ArrayList<InputDevice>();
    for (var eventDeviceFile : eventDeviceFiles) {
      var path = eventDeviceFile.getAbsolutePath();
      var existingDevice = this.nativeDevices.get(path);
      if (existingDevice != null) {
        refreshedDevices.add(existingDevice.inputDevice);
        continue;
      }

      var newDevice = initSingleDevice(eventDeviceFile);
      if (newDevice != null) {
        this.nativeDevices.put(newDevice.inputDevice.getID(), newDevice);
        refreshedDevices.add(newDevice.inputDevice);
      }
    }

    return refreshedDevices;
  }


  /**
   * Normalize the input value to the range [-1, 1] for axes.
   * <p>
   * The value is normalized to the range [-1, 1] for axes.
   * The value is 0 or 1 for buttons and non-axis D-Pad components.
   * </p>
   */
  static float normalizeInputValue(input_event inputEvent, LinuxEventComponent nativeComponent) {
    float value = inputEvent.value;
    if (nativeComponent.nativeType == LinuxEventDevice.EV_ABS) {
      if (nativeComponent.min == nativeComponent.max) {
        return 0f;
      }
      value = Math.max(nativeComponent.min, Math.min(nativeComponent.max, value));
      value = (value - nativeComponent.min) / (float) (nativeComponent.max - nativeComponent.min);
      if (!nativeComponent.isOneSided()) {
        value = value * 2 - 1;
      }
      if (!isStickAxis(nativeComponent)) {
        value = AbstractInputDevicePlugin.applyDeadzone(value, nativeComponent.normalizedDeadzone());
      }
    }

    if (nativeComponent.nativeType == LinuxEventDevice.EV_KEY) {
      value = value == 0 ? 0 : 1;
    }

    return value;
  }

  private static boolean isStickAxis(LinuxEventComponent component) {
    var identifier = component.getIdentifier();
    return identifier.equals(Axis.AXIS_X) || identifier.equals(Axis.AXIS_Y)
        || identifier.equals(Axis.AXIS_RX) || identifier.equals(Axis.AXIS_RY);
  }

  /**
   * Get the component index by the native ID.
   * <p>
   * The native ID is the code of the input event.
   * </p>
   *
   * @return the index of the component in the input device or <c>Linux.ERROR</c> if the component is not found
   */
  static int getComponentIndexByNativeId(input_event inputEvent, InputDevice inputDevice) {
    for (int j = 0; j < inputDevice.getComponents().size(); j++) {
      var component = inputDevice.getComponents().get(j);

      if (component.getType() != ComponentType.UNKNOWN) {
        switch (inputEvent.type) {
          case LinuxEventDevice.EV_KEY:
            if (component.getType() != ComponentType.BUTTON) {
              continue;
            }
            break;
          case LinuxEventDevice.EV_ABS:
            if (component.getType() != ComponentType.AXIS) {
              continue;
            }
            break;
        }
      }

      if (component.getId().nativeId == inputEvent.code) {
        return j;
      }
    }

    return Linux.ERROR;
  }

  static boolean isIgnoredDeviceName(String name) {
    if (name == null) {
      return false;
    }
    var upper = name.toUpperCase();
    return upper.contains("VIDEO BUS")
        || upper.contains("VIRTUAL")
        || upper.contains("POWER BUTTON")
        || upper.contains("HDA INTEL")
        || upper.contains("HDMI");
  }

  private LinuxEventDevice initSingleDevice(File eventDeviceFile) {
    String path = eventDeviceFile.getAbsolutePath();

    // 1. Probe candidate node using a short-lived confined arena
    byte[] probeKeyBits;
    byte[] probeAbsBits;
    try (Arena probeArena = Arena.ofConfined()) {
      int probeFd = Linux.open(probeArena, path);
      if (probeFd == Linux.ERROR) {
        log.log(Level.INFO, "Could not open device (permission denied): {0}", path);
        return null;
      }

      try {
        String deviceName = Linux.getEventDeviceName(probeArena, probeFd);
        if (isIgnoredDeviceName(deviceName)) {
          log.log(Level.FINE, "Ignoring virtual device: {0}", deviceName);
          return null;
        }

        byte[] eventTypes = Linux.getBits(probeArena, LinuxEventDevice.EV_SYN, probeFd);
        if (eventTypes == null) {
          log.log(Level.SEVERE, "Failed to get event types for {0}", path);
          return null;
        }

        probeKeyBits = LinuxEventDevice.isBitSet(eventTypes, LinuxEventDevice.EV_KEY)
            ? Linux.getBits(probeArena, LinuxEventDevice.EV_KEY, probeFd) : null;
        probeAbsBits = LinuxEventDevice.isBitSet(eventTypes, LinuxEventDevice.EV_ABS)
            ? Linux.getBits(probeArena, LinuxEventDevice.EV_ABS, probeFd) : null;

        if (!isGamepadOrJoystick(probeKeyBits, probeAbsBits)) {
          log.log(Level.FINE, "Ignoring non-gamepad device: {0} ({1})", new Object[] {deviceName, path});
          return null;
        }
      } finally {
        Linux.close(probeArena, probeFd);
      }
    }

    // 2. Candidate confirmed as gamepad/joystick! Allocate dedicated arena for its lifetime.
    LinuxEventDevice device = new LinuxEventDevice(path, true);
    if (device.fd == Linux.ERROR) {
      device.close();
      return null;
    }

    if (device.openedReadOnly) {
      log.log(Level.INFO, "Device opened read-only (no force feedback): {0}", device.filename);
    }

    if (device.supportsForceFeedback) {
      log.log(Level.FINE, "Device supports force feedback: {0} with {1} effects",
          new Object[] {device.name, device.maxEffects});
      if (device.supportsGain) {
        Linux.setGain(device.arena, device.fd, MAX_MAGNITUDE);
      }
    }

    int vendorId = device.id != null ? Short.toUnsignedInt(device.id.vendor) : -1;
    int productId = device.id != null ? Short.toUnsignedInt(device.id.product) : -1;
    String displayName = de.gurkenlabs.input4j.ControllerDatabase.getDisplayName(vendorId, productId);

    var inputDevice = new InputDevice(path, device.name, device.name, vendorId, productId, displayName,
        this::pollLinuxEventDevice, this::rumbleLinuxEventDevice, this::getBatteryInfo);
    device.inputDevice = inputDevice;

    if (probeKeyBits != null) {
      addEventComponents(device.arena, device, inputDevice, probeKeyBits, LinuxEventDevice.EV_KEY, LinuxEventDevice.KEY_MAX, "EV_KEY");
    }
    if (probeAbsBits != null) {
      addEventComponents(device.arena, device, inputDevice, probeAbsBits, LinuxEventDevice.EV_ABS, LinuxEventDevice.ABS_MAX, "EV_ABS");
    }

    // ignore devices without components
    if (device.componentList.isEmpty() || device.componentList.stream().noneMatch(x -> x.componentType == ComponentType.BUTTON || x.componentType == ComponentType.AXIS)) {
      device.close();
      return null;
    }

    LinuxVirtualComponentHandler.prepareVirtualComponents(device.inputDevice, inputDevice.getComponents());
    String accessMode = device.supportsForceFeedback ? "full" : "read-only";
    log.log(Level.INFO, "Found input device: {0} - {1} ({2}) with {3} components",
        new Object[] {device.filename, device.name, accessMode, device.componentList.size()});
    return device;
  }

  private void addEventComponents(Arena memoryArena, LinuxEventDevice device, InputDevice inputDevice, byte[] components, int eventType, int max, String componentType) {
    int vendorId = device.id != null ? Short.toUnsignedInt(device.id.vendor) : -1;
    int productId = device.id != null ? Short.toUnsignedInt(device.id.product) : -1;
    String deviceName = device.name;

    for (int i = 0; i < max; i++) {
      if (LinuxEventDevice.isBitSet(components, i)) {
        LinuxEventComponent nativeComponent;
        if (eventType == LinuxEventDevice.EV_ABS) {
          input_absinfo absInfo = Linux.getAbsInfo(memoryArena, device.fd, i);
          if (absInfo == null) {
            nativeComponent = new LinuxEventComponent(eventType, i, vendorId, productId, deviceName);
          } else {
            nativeComponent = new LinuxEventComponent(eventType, i, absInfo, vendorId, productId, deviceName);
          }
        } else {
          nativeComponent = new LinuxEventComponent(eventType, i, vendorId, productId, deviceName);
        }

        device.componentList.add(nativeComponent);

        var id = nativeComponent.getIdentifier();
        var inputComponent = new InputComponent(inputDevice, id, nativeComponent.linuxComponentType.name(), nativeComponent.relative);
        nativeComponent.inputComponent = inputComponent;
        inputDevice.addComponent(inputComponent);
      }
    }
  }

  /**
   * Processes input events, excluding EV_MSC and EV_SYN events.
   * <p>
   * EV_MSC events provide extra device-specific information (e.g., scan codes) and
   * EV_SYN events mark the end of an event batch for synchronization. Although these
   * events are necessary for the low-level input system, they are not needed for the
   * core event handling logic in this method.
   * </p>
   */
  private float[] pollLinuxEventDevice(InputDevice inputDevice) {
    this.refreshDevices();

    var emptyValues = new float[inputDevice.getComponents().size()];

    // find native LinuxEventDevice and poll it
    var linuxEventDevice = this.nativeDevices.getOrDefault(inputDevice.getID(), null);
    if (linuxEventDevice == null || linuxEventDevice.isDisconnected) {
      log.log(Level.WARNING, "LinuxEventDevice not found for input device " + inputDevice.getName());
      return emptyValues;
    }

    // use the last polled values since we need to keep the state of the buttons and axes until they are released
    if (linuxEventDevice.currentValues == null) {
      linuxEventDevice.currentValues = emptyValues;
    }

    linuxEventDevice.pollErrno[0] = 0;
    input_event inputEvent;
    while ((inputEvent = linuxEventDevice.readEvent(linuxEventDevice.pollErrno)) != null) {
      if (inputEvent.type == LinuxEventDevice.EV_SYN
        || inputEvent.type == LinuxEventDevice.EV_MSC
        || inputEvent.type == LinuxEventDevice.EV_REL) {
        continue;
      }

      var nativeComponent = linuxEventDevice.getNativeComponent(inputEvent);
      if (nativeComponent == null) {
        log.log(Level.SEVERE, "Failed to find component for " + inputEvent.type + " " + inputEvent.code);
        continue;
      }

      int componentIndex = linuxEventDevice.componentList.indexOf(nativeComponent);
      if (componentIndex == -1) {
        log.log(Level.SEVERE, "Failed to find component index for " + inputEvent.type + " " + inputEvent.code);
        continue;
      }

      linuxEventDevice.currentValues[componentIndex] = normalizeInputValue(inputEvent, nativeComponent);
    }

    if (linuxEventDevice.pollErrno[0] == Linux.ENODEV || linuxEventDevice.pollErrno[0] == Linux.EBADF) {
      linuxEventDevice.isDisconnected = true;
      this.refreshDevices(true);
      return emptyValues;
    }

    var polledValues = linuxEventDevice.currentValues.clone();
    applyCircularDeadzone(linuxEventDevice, polledValues, Axis.AXIS_X, Axis.AXIS_Y);
    applyCircularDeadzone(linuxEventDevice, polledValues, Axis.AXIS_RX, Axis.AXIS_RY);
    return LinuxVirtualComponentHandler.handlePolledValues(inputDevice, polledValues);
  }

  private static void applyCircularDeadzone(
      LinuxEventDevice device, float[] values, InputComponent.ID xAxis, InputComponent.ID yAxis) {
    var xIndex = findAxisIndex(device, xAxis);
    var yIndex = findAxisIndex(device, yAxis);
    if (xIndex < 0 || yIndex < 0) {
      return;
    }

    var deadzone = Math.max(device.componentList.get(xIndex).normalizedDeadzone(),
        device.componentList.get(yIndex).normalizedDeadzone());
    AbstractInputDevicePlugin.applyCircularDeadzone(values, xIndex, yIndex, deadzone);
  }

  private static int findAxisIndex(LinuxEventDevice device, InputComponent.ID axis) {
    for (int i = 0; i < device.componentList.size(); i++) {
      if (axis.equals(device.componentList.get(i).getIdentifier())) {
        return i;
      }
    }
    return -1;
  }

  private static final float RUMBLE_THRESHOLD = 0.01f;
  private static final int MAX_MAGNITUDE = 65535;

  private final ff_effect rumbleEffectTemplate = createRumbleEffectTemplate();
  private final ff_effect sineEffectTemplate = createSineEffectTemplate();
  private final input_event playEventTemplate = new input_event();
  private final input_event stopEventTemplate = new input_event();

  private ff_effect createRumbleEffectTemplate() {
    var effect = new ff_effect();
    effect.type = Linux.FF_RUMBLE;
    effect.id = -1;
    effect.direction = 0;
    effect.trigger = new ff_trigger();
    effect.trigger.button = 0;
    effect.trigger.interval = 0;
    effect.replay = new ff_replay();
    effect.replay.length = 0;
    effect.replay.delay = 0;
    effect.rumble = new ff_rumble_effect();
    return effect;
  }

  private ff_effect createSineEffectTemplate() {
    var effect = new ff_effect();
    effect.type = Linux.FF_PERIODIC;
    effect.id = -1;
    effect.direction = 0;
    effect.trigger = new ff_trigger();
    effect.trigger.button = 0;
    effect.trigger.interval = 0;
    effect.replay = new ff_replay();
    effect.replay.length = 0;
    effect.replay.delay = 0;
    effect.periodic = new ff_periodic_effect();
    effect.periodic.waveform = Linux.FF_SINE;
    effect.periodic.period = 50;
    effect.periodic.offset = 0;
    effect.periodic.phase = 0;
    effect.periodic.envelope = new ff_envelope();
    effect.periodic.envelope.attack_length = 0;
    effect.periodic.envelope.attack_level = 0;
    effect.periodic.envelope.fade_length = 0;
    effect.periodic.envelope.fade_level = 0;
    return effect;
  }

  /**
   * Sets the rumble (force feedback) intensity for the input device.
   *
   * @param inputDevice the input device
   * @param intensity   the intensity values. intensity[0] is the strong motor,
   *                    intensity[1] (optional) is the weak motor.
   *                    Values should be in range 0.0 to 1.0.
   */
  private void rumbleLinuxEventDevice(InputDevice inputDevice, float[] intensity) {
    var linuxEventDevice = this.nativeDevices.getOrDefault(inputDevice.getID(), null);
    if (linuxEventDevice == null || linuxEventDevice.isDisconnected) {
      log.log(Level.WARNING, "LinuxEventDevice not found for input device " + inputDevice.getName());
      return;
    }

    if (!linuxEventDevice.supportsForceFeedback) {
      if (linuxEventDevice.openedReadOnly) {
        log.log(Level.WARNING, "Rumble not supported - device opened read-only (requires write access): {0}", linuxEventDevice.filename);
      } else if (!linuxEventDevice.supportsRumble && !linuxEventDevice.supportsSine) {
        log.log(Level.WARNING, "Rumble not supported - device does not report FF_RUMBLE or FF_SINE capability: {0}", linuxEventDevice.name);
      }
      return;
    }

    if (linuxEventDevice.fd == Linux.ERROR) {
      return;
    }

    if (intensity == null || intensity.length == 0 || (intensity.length > 0 && intensity[0] < RUMBLE_THRESHOLD && (intensity.length == 1 || intensity[1] < RUMBLE_THRESHOLD))) {
      stopRumble(linuxEventDevice);
      return;
    }

    float strongMagnitude = Math.clamp(intensity[0], 0f, 1f);
    float weakMagnitude = intensity.length > 1 ? Math.clamp(intensity[1], 0f, 1f) : strongMagnitude;

    try (Arena rumbleArena = Arena.ofConfined()) {
      if (linuxEventDevice.currentEffectId != -1) {
        if (Math.abs(linuxEventDevice.currentStrongMagnitude - strongMagnitude) >= RUMBLE_THRESHOLD
            || Math.abs(linuxEventDevice.currentWeakMagnitude - weakMagnitude) >= RUMBLE_THRESHOLD) {
          Linux.removeEffect(rumbleArena, linuxEventDevice.fd, linuxEventDevice.currentEffectId);
          linuxEventDevice.currentEffectId = -1;
        } else {
          playEventTemplate.type = (short) LinuxEventDevice.EV_FF;
          playEventTemplate.code = (short) linuxEventDevice.currentEffectId;
          playEventTemplate.value = 1;

          Linux.writeEvent(rumbleArena, linuxEventDevice.fd, playEventTemplate);
          return;
        }
      }

      if (linuxEventDevice.supportsRumble) {
        rumbleEffectTemplate.rumble.strong_magnitude = (short) (strongMagnitude * MAX_MAGNITUDE);
        rumbleEffectTemplate.rumble.weak_magnitude = (short) (weakMagnitude * MAX_MAGNITUDE);

        int effectId = Linux.uploadEffect(rumbleArena, linuxEventDevice.fd, rumbleEffectTemplate);
        if (effectId == Linux.ERROR) {
          log.log(Level.WARNING, "Failed to upload rumble effect for device " + inputDevice.getName());
          return;
        }

        linuxEventDevice.currentEffectId = effectId;
        linuxEventDevice.currentStrongMagnitude = strongMagnitude;
        linuxEventDevice.currentWeakMagnitude = weakMagnitude;

        playEventTemplate.type = (short) LinuxEventDevice.EV_FF;
        playEventTemplate.code = (short) effectId;
        playEventTemplate.value = 1;

        int result = Linux.writeEvent(rumbleArena, linuxEventDevice.fd, playEventTemplate);
        if (result == Linux.ERROR) {
          log.log(Level.WARNING, "Failed to play rumble effect for device " + inputDevice.getName());
          Linux.removeEffect(rumbleArena, linuxEventDevice.fd, effectId);
          linuxEventDevice.currentEffectId = -1;
        }
      } else {
        int magnitude = (int) (strongMagnitude * MAX_MAGNITUDE / 3 + weakMagnitude * MAX_MAGNITUDE / 6);
        sineEffectTemplate.periodic.magnitude = (short) magnitude;

        int effectId = Linux.uploadEffect(rumbleArena, linuxEventDevice.fd, sineEffectTemplate);
        if (effectId == Linux.ERROR) {
          log.log(Level.WARNING, "Failed to upload sine fallback effect for device " + inputDevice.getName());
          return;
        }

        linuxEventDevice.currentEffectId = effectId;
        linuxEventDevice.currentStrongMagnitude = strongMagnitude;
        linuxEventDevice.currentWeakMagnitude = weakMagnitude;

        playEventTemplate.type = (short) LinuxEventDevice.EV_FF;
        playEventTemplate.code = (short) effectId;
        playEventTemplate.value = 1;

        int result = Linux.writeEvent(rumbleArena, linuxEventDevice.fd, playEventTemplate);
        if (result == Linux.ERROR) {
          log.log(Level.WARNING, "Failed to play sine fallback effect for device " + inputDevice.getName());
          Linux.removeEffect(rumbleArena, linuxEventDevice.fd, effectId);
          linuxEventDevice.currentEffectId = -1;
        }
      }
    }
  }

  private void stopRumble(LinuxEventDevice linuxEventDevice) {
    if (linuxEventDevice.currentEffectId == -1) {
      return;
    }

    if (linuxEventDevice.fd != Linux.ERROR) {
      try (Arena stopArena = Arena.ofConfined()) {
        stopEventTemplate.type = (short) LinuxEventDevice.EV_FF;
        stopEventTemplate.code = (short) linuxEventDevice.currentEffectId;
        stopEventTemplate.value = 0;

        Linux.writeEvent(stopArena, linuxEventDevice.fd, stopEventTemplate);

        Linux.removeEffect(stopArena, linuxEventDevice.fd, linuxEventDevice.currentEffectId);
      }
    }
    linuxEventDevice.currentEffectId = -1;
    linuxEventDevice.currentStrongMagnitude = 0f;
    linuxEventDevice.currentWeakMagnitude = 0f;
  }

  private BatteryInfo getBatteryInfo(InputDevice inputDevice) {
    var device = nativeDevices.get(inputDevice.getID());
    if (device == null || device.isDisconnected || device.id == null) {
      return null;
    }

    int vendorId = Short.toUnsignedInt(device.id.vendor);
    int productId = Short.toUnsignedInt(device.id.product);

    String batteryPath = findBatteryPath(vendorId, productId);
    if (batteryPath == null) {
      return null;
    }

    try {
      int percentage = readBatteryPercentage(batteryPath);
      if (percentage < 0) {
        return null;
      }

      return BatteryInfo.fromPercentage(BatteryType.UNKNOWN, false, percentage);
    } catch (Exception e) {
      log.log(Level.FINE, "Failed to read battery for device " + device.name, e);
      return null;
    }
  }

  private String findBatteryPath(int vendorId, int productId) {
    File powerSupplyDir = new File("/sys/class/power_supply");
    if (!powerSupplyDir.exists() || !powerSupplyDir.isDirectory()) {
      return null;
    }

    String vendorHex = String.format("%04x", vendorId);
    String productHex = String.format("%04x", productId);

    File[] entries = powerSupplyDir.listFiles();
    if (entries == null) {
      return null;
    }

    for (File entry : entries) {
      if (!entry.isDirectory()) {
        continue;
      }

      try {
        String type = readSysfsFile(entry, "type");
        if (type == null || !type.equalsIgnoreCase("Battery")) {
          continue;
        }

        String vendor = readSysfsFile(entry, "vendor");
        String manufacturer = readSysfsFile(entry, "manufacturer");
        String model = readSysfsFile(entry, "model_name");

        boolean match = false;
        if (vendor != null && vendor.toLowerCase().contains(vendorHex)) {
          match = true;
        }
        if (manufacturer != null && manufacturer.toLowerCase().contains(vendorHex)) {
          match = true;
        }
        if (model != null && model.toLowerCase().contains(productHex)) {
          match = true;
        }

        if (match) {
          return entry.getAbsolutePath();
        }
      } catch (IOException e) {
        continue;
      }
    }

    return null;
  }

  private String readSysfsFile(File dir, String filename) throws IOException {
    File file = new File(dir, filename);
    if (file.exists()) {
      return Files.readString(file.toPath()).trim();
    }
    return null;
  }

  private int readBatteryPercentage(String batteryPath) throws IOException {
    File capacityFile = new File(batteryPath, "capacity");
    if (capacityFile.exists()) {
      String content = Files.readString(capacityFile.toPath()).trim();
      return Integer.parseInt(content);
    }

    File capacityLevelFile = new File(batteryPath, "capacity_level");
    if (capacityLevelFile.exists()) {
      String level = Files.readString(capacityLevelFile.toPath()).trim().toLowerCase();
      return switch (level) {
        case "full" -> 100;
        case "high", "normal" -> 75;
        case "low" -> 25;
        case "critical" -> 10;
        default -> -1;
      };
    }

    return -1;
  }

  /**
   * Determines whether the given event bitmasks represent a gamepad, joystick, or flight controller
   * rather than a mouse, touchpad, touchscreen, or keyboard.
   *
   * @param keyBits the EV_KEY bitmask, or null
   * @param absBits the EV_ABS bitmask, or null
   * @return true if the device is identified as a gamepad/joystick, false otherwise
   */
  static boolean isGamepadOrJoystick(byte[] keyBits, byte[] absBits) {
    boolean hasGamepadButtons = false;
    if (keyBits != null) {
      // BTN_MISC (0x100 - 0x109: BTN_0..BTN_9)
      for (int btn = LinuxInputDefinitions.BTN_0; btn <= LinuxInputDefinitions.BTN_9; btn++) {
        if (LinuxEventDevice.isBitSet(keyBits, btn)) {
          hasGamepadButtons = true;
          break;
        }
      }
      // BTN_JOYSTICK (0x120 - 0x12f: BTN_TRIGGER, BTN_THUMB, etc.)
      if (!hasGamepadButtons) {
        for (int btn = LinuxInputDefinitions.BTN_JOYSTICK; btn <= LinuxInputDefinitions.BTN_DEAD; btn++) {
          if (LinuxEventDevice.isBitSet(keyBits, btn)) {
            hasGamepadButtons = true;
            break;
          }
        }
      }
      // BTN_GAMEPAD (0x130 - 0x13e: BTN_A..BTN_THUMBR)
      if (!hasGamepadButtons) {
        for (int btn = LinuxInputDefinitions.BTN_GAMEPAD; btn <= LinuxInputDefinitions.BTN_THUMBR; btn++) {
          if (LinuxEventDevice.isBitSet(keyBits, btn)) {
            hasGamepadButtons = true;
            break;
          }
        }
      }
      // BTN_WHEEL (0x150, 0x151: BTN_GEAR_DOWN, BTN_GEAR_UP)
      if (!hasGamepadButtons
          && (LinuxEventDevice.isBitSet(keyBits, LinuxInputDefinitions.BTN_GEAR_DOWN)
              || LinuxEventDevice.isBitSet(keyBits, LinuxInputDefinitions.BTN_GEAR_UP))) {
        hasGamepadButtons = true;
      }
      // BTN_DPAD_UP..BTN_DPAD_RIGHT (0x220 - 0x223)
      if (!hasGamepadButtons) {
        for (int btn = 0x220; btn <= 0x223; btn++) {
          if (LinuxEventDevice.isBitSet(keyBits, btn)) {
            hasGamepadButtons = true;
            break;
          }
        }
      }
      // BTN_TRIGGER_HAPPY (0x2c0 - 0x2e7)
      if (!hasGamepadButtons) {
        for (int btn = LinuxInputDefinitions.BTN_TRIGGER_HAPPY1; btn <= LinuxInputDefinitions.BTN_TRIGGER_HAPPY40; btn++) {
          if (LinuxEventDevice.isBitSet(keyBits, btn)) {
            hasGamepadButtons = true;
            break;
          }
        }
      }
    }

    if (hasGamepadButtons) {
      return true;
    }

    // Reject touchpads, touchscreens, and digitizer tools when lacking gamepad buttons
    if (keyBits != null) {
      boolean hasTouchOrTool = LinuxEventDevice.isBitSet(keyBits, LinuxInputDefinitions.BTN_TOUCH)
          || LinuxEventDevice.isBitSet(keyBits, LinuxInputDefinitions.BTN_TOOL_FINGER)
          || LinuxEventDevice.isBitSet(keyBits, LinuxInputDefinitions.BTN_TOOL_DOUBLETAP)
          || LinuxEventDevice.isBitSet(keyBits, LinuxInputDefinitions.BTN_TOOL_TRIPLETAP)
          || LinuxEventDevice.isBitSet(keyBits, LinuxInputDefinitions.BTN_TOOL_QUADTAP)
          || LinuxEventDevice.isBitSet(keyBits, LinuxInputDefinitions.BTN_TOOL_PEN);
      if (hasTouchOrTool) {
        return false;
      }

      // Reject mice when lacking gamepad buttons (BTN_LEFT..BTN_TASK: 0x110..0x117)
      boolean hasMouseButtons = false;
      for (int btn = LinuxInputDefinitions.BTN_MOUSE; btn <= LinuxInputDefinitions.BTN_TASK; btn++) {
        if (LinuxEventDevice.isBitSet(keyBits, btn)) {
          hasMouseButtons = true;
          break;
        }
      }
      if (hasMouseButtons) {
        return false;
      }
    }

    // Accept joysticks, rudder pedals, flight sticks, and racing wheels without buttons
    if (absBits != null) {
      for (int axis = LinuxInputDefinitions.ABS_HAT0X; axis <= LinuxInputDefinitions.ABS_HAT3Y; axis++) {
        if (LinuxEventDevice.isBitSet(absBits, axis)) {
          return true;
        }
      }
      if (LinuxEventDevice.isBitSet(absBits, LinuxInputDefinitions.ABS_THROTTLE)
          || LinuxEventDevice.isBitSet(absBits, LinuxInputDefinitions.ABS_RUDDER)
          || LinuxEventDevice.isBitSet(absBits, LinuxInputDefinitions.ABS_WHEEL)
          || LinuxEventDevice.isBitSet(absBits, LinuxInputDefinitions.ABS_GAS)
          || LinuxEventDevice.isBitSet(absBits, LinuxInputDefinitions.ABS_BRAKE)
          || LinuxEventDevice.isBitSet(absBits, LinuxInputDefinitions.ABS_RX)
          || LinuxEventDevice.isBitSet(absBits, LinuxInputDefinitions.ABS_RY)
          || LinuxEventDevice.isBitSet(absBits, LinuxInputDefinitions.ABS_RZ)) {
        return true;
      }
    }

    return false;
  }
}
