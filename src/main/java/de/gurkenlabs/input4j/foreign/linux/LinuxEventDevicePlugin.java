package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
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
 */
public class LinuxEventDevicePlugin extends AbstractInputDevicePlugin {
  private static final Logger log = Logger.getLogger(LinuxEventDevicePlugin.class.getName());

  private final Arena memoryArena = Arena.ofConfined();
  private final Collection<LinuxEventDevice> devices = ConcurrentHashMap.newKeySet();
  private volatile boolean stop = false;

  @Override
  public void internalInitDevices(Frame owner) {
    enumEventDevices();

    // The joystick API (/dev/input/jsX) is considered legacy and is no longer actively developed.
    // The evdev API (/dev/input/eventX) has largely replaced it because it is more flexible and supports additional features like force feedback.
    // Reasons to use evdev over the joystick API:
    // - evdev supports reconfiguration, multi-device support, and extensibility.
    // - evdev is preferred by newer software and libraries like SDL, libevdev, and udev.
    // - evdev supports force feedback, which is not available in the joystick API.
    // While the joystick API will likely remain for legacy software, modern software should migrate to evdev for better compatibility and future-proofing.
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
      if (device.fd == Linux.ERROR || device.epfd == Linux.ERROR) {
        log.log(Level.SEVERE, "Failed to open " + eventDeviceFile.getAbsolutePath());
        continue;
      }

      // ignore some devices since they are not useful for input
      if (device.name != null
              && (device.name.toUpperCase().contains("VIDEO BUS")
              || device.name.toUpperCase().contains("VIRTUAL")
              || device.name.toUpperCase().contains("POWER BUTTON"))) {
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
      addEventComponents(memoryArena, device, inputDevice, eventTypes, LinuxEventDevice.EV_REL, LinuxEventDevice.REL_MAX, "EV_REL");

      // ignore devices without components
      if (device.componentList.isEmpty()) {
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
          var nativeComponent = new LinuxEventComponent(memoryArena, device, eventType, i);
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

  private float[] pollLinuxEventDevice(InputDevice inputDevice) {
    var polledValues = new float[inputDevice.getComponents().size()];

    // Rewrite this to use epoll_wait instead of polling each device

    // find native LinuxEventDevice and poll it
    var linuxEventDevice = this.devices.stream().filter(x -> x.inputDevice.equals(inputDevice)).findFirst().orElse(null);
    if (linuxEventDevice == null) {
      log.log(Level.WARNING, "LinuxEventDevice not found for input device " + inputDevice.getInstanceName());
      return polledValues;
    }

    int numEvents = Linux.epollWait(this.memoryArena, linuxEventDevice.epfd, linuxEventDevice.componentList.size());
    if (numEvents == 0) {
      return polledValues;
    } else if (numEvents == Linux.ERROR) {
      log.log(Level.SEVERE, "epoll_wait failed for " + linuxEventDevice.filename);
      return polledValues;
    }

    for(int i = 0; i < numEvents; i++) {
      var inputEvent = Linux.read(this.memoryArena, linuxEventDevice.fd);
      if (inputEvent != null) {
        // log.log(Level.INFO, "Key " + inputEvent.code + " " + (inputEvent.value != 0 ? "pressed" : "released"));
        // TODO: normalize the value to a float, find the component index for the event code (this might not be in order)
        polledValues[i] = inputEvent.value;
      }
    }

    return polledValues;
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