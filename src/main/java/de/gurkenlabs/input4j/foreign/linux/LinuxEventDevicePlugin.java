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
 * <p>
 * TODO: Test this on raspberrypi3b with SNES USB controller and old controller
 */
public class LinuxEventDevicePlugin extends AbstractInputDevicePlugin {
  private static final Logger log = Logger.getLogger(LinuxEventDevicePlugin.class.getName());

  private static final int EVIOCGRAB = 0x40044590;

  private static final int SYN_MT_REPORT = 0x02;
  private static final int SYN_DROPPED = 0x03;
  private static final int EV_MSC = 0x04;
  private static final int MSC_RAW = 0x03;
  private static final int MSC_SCAN = 0x04;

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

      var inputDevice = new InputDevice(device.name, device.name, this::pollLinuxEventDevice, this::rumbleLinuxEventDevice);
      device.inputDevice = inputDevice;

      log.log(Level.INFO, "Found input device: " + device.filename + " - " + device.name);

      // Check for available event types
      byte[] eventTypes = Linux.getBits(this.memoryArena, LinuxEventDevice.EV_SYN, device.fd);
      if (eventTypes == null) {
        log.log(Level.SEVERE, "Failed to get event types for " + device.filename);
        continue;
      }

      // Check for available components per event type (EV_KEY, EV_ABS, EV_REL, etc.)
      addComponents(memoryArena, device, inputDevice, eventTypes, LinuxEventDevice.EV_KEY, LinuxEventDevice.KEY_MAX, "EV_KEY");
      addComponents(memoryArena, device, inputDevice, eventTypes, LinuxEventDevice.EV_ABS, LinuxEventDevice.ABS_MAX, "EV_ABS");
      addComponents(memoryArena, device, inputDevice, eventTypes, LinuxEventDevice.EV_REL, LinuxEventDevice.REL_MAX, "EV_REL");

      this.devices.add(device);
    }
  }

  private void addComponents(Arena memoryArena, LinuxEventDevice device, InputDevice inputDevice, byte[] eventTypes, int eventType, int max, String componentType) {
    if (LinuxEventDevice.isBitSet(eventTypes, eventType)) {
      byte[] components = Linux.getBits(memoryArena, eventType, device.fd);
      if (components == null) {
        log.log(Level.SEVERE, "Failed to get " + componentType + " components for " + device.filename);
        return;
      }

      for (int i = 0; i < max; i++) {
        if (LinuxEventDevice.isBitSet(components, i)) {
          var component = new LinuxEventComponent(memoryArena, device, eventType, i);
          inputDevice.addComponent(new InputComponent(inputDevice, component.componentType, component.linuxComponentType.getIdentifier(i), component.relative));
        }
      }
    }
  }

  private float[] pollLinuxEventDevice(InputDevice inputDevice) {
    var polledValues = new float[inputDevice.getComponents().size()];

    // find native LinuxEventDevice and poll it
    var linuxEventDevice = this.devices.stream().filter(x -> x.inputDevice.equals(inputDevice)).findFirst().orElse(null);
    if (linuxEventDevice == null) {
      log.log(Level.WARNING, "LinuxEventDevice not found for input device " + inputDevice.getInstanceName());
      return polledValues;
    }

    // TODO: assign the button states from this
    var keyState = Linux.getKeyStates(this.memoryArena, linuxEventDevice.fd);
    for(var key : keyState) {
      if(key) {
        log.log(Level.WARNING, "Key is pressed");
      }
    }
    input_event nextInputEvent;
    do{
      nextInputEvent= Linux.readEvent(this.memoryArena, linuxEventDevice.fd);
      if(nextInputEvent != null){
        log.log(Level.INFO, "Event type: " + nextInputEvent.type + " Code: " + nextInputEvent.code + " Value: " + nextInputEvent.value);
      }
    } while(nextInputEvent != null);

    return new float[0];
  }

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