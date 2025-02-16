package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;

import java.awt.*;
import java.io.File;
import java.lang.foreign.Arena;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code LinuxEventDevicePlugin} class is responsible for managing Linux event devices.
 * It initializes and adds them to the collection of devices.
 * <p>
 *  The joystick API (/dev/input/jsX) is considered legacy and is no longer actively developed.
 *  The evdev API (/dev/input/eventX) has largely replaced it because it is more flexible and supports additional features like force feedback.
 *  Reasons to use evdev over the joystick API:
 *  <ul>
 *    <li>evdev is the modern Linux input API and is actively developed.</li>
 *    <li>evdev is more flexible and supports additional features like force feedback.</li>
 *    <li>evdev is the preferred API for newer software and libraries like SDL, libevdev, and udev.</li>
 *  </ul>
 */
public class LinuxEventDevicePlugin extends AbstractInputDevicePlugin {
  private static final Logger log = Logger.getLogger(LinuxEventDevicePlugin.class.getName());

  private final Arena memoryArena = Arena.ofConfined();
  private final Collection<LinuxEventDevice> devices = ConcurrentHashMap.newKeySet();
  private volatile boolean stop = false;

  @Override
  public void internalInitDevices(Frame owner) {
    enumEventDevices();
  }

  private void enumEventDevices() {
    final File dev = new File("/dev/input");
    File[] eventDeviceFiles = dev.listFiles((File dir, String name) -> name.startsWith("event"));
    if (eventDeviceFiles == null) {
      log.log(Level.SEVERE, "No event devices found");
      return;
    } else {
      Arrays.sort(eventDeviceFiles, Comparator.comparing(File::getName));
    }

    for (var eventDeviceFile : eventDeviceFiles) {
      LinuxEventDevice device = new LinuxEventDevice(this.memoryArena, eventDeviceFile.getAbsolutePath());
      if (device.fd == Linux.ERROR) {
        log.log(Level.SEVERE, "Failed to open " + eventDeviceFile.getAbsolutePath());
        continue;
      }

      // ignore some devices since they are not useful for input
      if (device.name != null
              && (device.name.toUpperCase().contains("VIDEO BUS")
              || device.name.toUpperCase().contains("VIRTUAL")
              || device.name.toUpperCase().contains("POWER BUTTON")
              || device.name.toUpperCase().contains("HDA INTEL")
              || device.name.toUpperCase().contains("HDMI"))) {
        log.log(Level.FINE, "Ignoring virtual device: " + device.name);
        continue;
      }

      var inputDevice = new InputDevice(device.name, device.name, this::pollLinuxEventDevice, this::rumbleLinuxEventDevice);
      device.inputDevice = inputDevice;

      // Check for available event types
      byte[] eventTypes = Linux.getBits(this.memoryArena, LinuxEventDevice.EV_SYN, device.fd);
      if (eventTypes == null) {
        log.log(Level.SEVERE, "Failed to get event types for " + device.filename);
        continue;
      }

      // Check for available components per event type (EV_KEY, EV_ABS, EV_REL, etc.)
      addEventComponents(memoryArena, device, inputDevice, eventTypes, LinuxEventDevice.EV_KEY, LinuxEventDevice.KEY_MAX, "EV_KEY");
      addEventComponents(memoryArena, device, inputDevice, eventTypes, LinuxEventDevice.EV_ABS, LinuxEventDevice.ABS_MAX, "EV_ABS");

      // ignore devices without components
      // also ignore devices that have no buttons, axis or dpad (this should also exclude keyboards)
      if (device.componentList.isEmpty() || device.componentList.stream().noneMatch(x -> x.componentType == ComponentType.Button || x.componentType == ComponentType.Axis || x.componentType == ComponentType.DPad)) {
        continue;
      }

      log.log(Level.INFO, "Found input device: " + device.filename + " - " + device.name + " with " + device.componentList.size() + " components");
      this.devices.add(device);
    }
  }

  private void addEventComponents(Arena memoryArena, LinuxEventDevice device, InputDevice inputDevice, byte[] eventTypes, int eventType, int max, String componentType) {
    if (LinuxEventDevice.isBitSet(eventTypes, eventType)) {
      byte[] components = Linux.getBits(memoryArena, eventType, device.fd);
      if (components == null) {
        log.log(Level.SEVERE, "Failed to get " + componentType + " components for " + device.filename);
        return;
      }

      for (int i = 0; i < max; i++) {
        if (LinuxEventDevice.isBitSet(components, i)) {
          LinuxEventComponent nativeComponent;
          if (eventType == LinuxEventDevice.EV_ABS) {
            // Get the absolute axis information if available (contains min, max, flat, fuzz, etc.)
            input_absinfo absInfo = Linux.getAbsInfo(memoryArena, device.fd, i);
            if (absInfo == null) {
              nativeComponent = new LinuxEventComponent(eventType, i);
            } else {
              nativeComponent = new LinuxEventComponent(eventType, i, absInfo);
            }
          } else {
            nativeComponent = new LinuxEventComponent(eventType, i);
          }

          device.componentList.add(nativeComponent);

          var id = nativeComponent.getIdentifier();
          var inputComponent = new InputComponent(inputDevice, id, nativeComponent.linuxComponentType.name(), nativeComponent.relative);
          nativeComponent.inputComponent = inputComponent;
          inputDevice.addComponent(inputComponent);
        }
      }

      // TODO: Linux evdev splits the D-Pad into ABS_HAT0X and ABS_HAT0Y for horizontal and vertical movements
      //     We need a unified virtual DPAD component to be in line with the other input libraries
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
    var emptyValues = new float[inputDevice.getComponents().size()];

    // find native LinuxEventDevice and poll it
    var linuxEventDevice = this.devices.stream().filter(x -> x.inputDevice.equals(inputDevice)).findFirst().orElse(null);
    if (linuxEventDevice == null) {
      log.log(Level.WARNING, "LinuxEventDevice not found for input device " + inputDevice.getInstanceName());
      return emptyValues;
    }

    // use the last polled values since we need to keep the state of the buttons and axes until they are released
    if (linuxEventDevice.currentValues == null) {
      linuxEventDevice.currentValues = emptyValues;
    }

    input_event inputEvent;
    while ((inputEvent = Linux.read(this.memoryArena, linuxEventDevice.fd)) != null) {
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

      var componentIndex = getComponentIndexByNativeId(inputEvent, inputDevice);
      if (componentIndex == Linux.ERROR) {
        log.log(Level.SEVERE, "Failed to find component for " + inputEvent.type + " " + inputEvent.code);
        continue;
      }

      linuxEventDevice.currentValues[componentIndex] = normalizeInputValue(inputEvent, nativeComponent);;
    }

    return linuxEventDevice.currentValues;
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
      int midpoint = Math.round((nativeComponent.min + nativeComponent.max) / 2.0f);
      if (inputEvent.value == nativeComponent.flat || Math.abs(inputEvent.value - midpoint) <= nativeComponent.fuzz) {
        value = 0;
      } else {
        // Ensure value is within the range [min, max]
        // Then normalize the value to the range [-1, 1]
        value = Math.max(nativeComponent.min, Math.min(nativeComponent.max, value));
        value = (value - nativeComponent.min) / (float) (nativeComponent.max - nativeComponent.min) * 2 - 1;
      }
    }

    if(nativeComponent.nativeType == LinuxEventDevice.EV_KEY) {
      value = value == 0 ? 0 : 1;
    }

    return value;
  }

  /**
   * Get the component index by the native ID.
   * <p>
   * The native ID is the code of the input event.
   * </p>
   * @return the index of the component in the input device or <c>Linux.ERROR</c> if the component is not found
   */
  static int getComponentIndexByNativeId(input_event inputEvent, InputDevice inputDevice) {
    for (int j = 0; j < inputDevice.getComponents().size(); j++) {
      var component = inputDevice.getComponents().get(j);

      if (component.getType() != ComponentType.Unknown) {
        switch (inputEvent.type) {
          case LinuxEventDevice.EV_KEY:
            if (component.getType() != ComponentType.Button && component.getType() != ComponentType.DPad) {
              continue;
            }
            break;
          case LinuxEventDevice.EV_ABS:
            if (component.getType() != ComponentType.Axis) {
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


  /**
   * TODO: Support for rumble and force feedback. ioctl(fd, EVIOCSFF, &effect) and requires ff_effect struct.
   */
  private void rumbleLinuxEventDevice(InputDevice inputDevice, float[] floats) {
  }

  @Override
  public Collection<InputDevice> getAll() {
    return this.devices.stream().map(x -> x.inputDevice).toList();
  }

  @Override
  public void close() {
    stop = true;
    for (LinuxEventDevice device : devices) {
      device.close(this.memoryArena);
    }
    memoryArena.close();
  }
}