package de.gurkenlabs.input4j.foreign.macos.iokit;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.InputDevice;

import java.awt.*;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class IOKitPlugin extends AbstractInputDevicePlugin {
  private static final Logger log = Logger.getLogger(IOKitPlugin.class.getName());

  private final Arena memoryArena = Arena.ofConfined();

  @Override
  public void internalInitDevices(Frame owner) {
    var devices = MacOS.getSupportedHIDDevices(memoryArena);
    for (var device : devices) {
      try {
        var pluginInterfaceSegment = this.memoryArena.allocate(IOCFPlugInInterface.$LAYOUT);
        var scoreSegment = this.memoryArena.allocate(JAVA_LONG);
        var pluginInterfaceReturn = MacOS.IOCreatePlugInInterfaceForService(device.deviceAddress, pluginInterfaceSegment, scoreSegment);
        if (pluginInterfaceReturn != IOReturn.kIOReturnSuccess) {
          log.log(Level.SEVERE, "Failed to create plugin interface: " + IOReturn.toString(pluginInterfaceReturn));
          continue;
        }

        var pluginInterface = IOCFPlugInInterface.read(pluginInterfaceSegment);
        var deviceInterfaceSegment = this.memoryArena.allocate(IOHIDDeviceInterface.$LAYOUT);

        var pluginReturn = pluginInterface.QueryInterface(MacOS.kIOHIDDeviceInterfaceID, deviceInterfaceSegment);
        if (pluginReturn != IOReturn.kIOReturnSuccess || deviceInterfaceSegment.equals(MemorySegment.NULL)) {
          log.log(Level.SEVERE, "Failed to query HID device interface: " + IOReturn.toString(pluginReturn));
          continue;
        }

        pluginInterface.release();

        device.deviceInterface = IOHIDDeviceInterface.read(deviceInterfaceSegment);
      } catch (Throwable e) {
        log.log(Level.SEVERE, "Failed to initialize HID device: " + e.getMessage());
      }
    }
  }

  @Override
  public Collection<InputDevice> getAll() {
    return List.of();
  }

  @Override
  public void close() {
    memoryArena.close();
  }
}
