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
    var hidMatchingDirectory = MacOS.IOServiceMatching(this.memoryArena);
    var ioiterator = this.memoryArena.allocate(JAVA_LONG);
    var ioMatchingServiceReturn = MacOS.IOServiceGetMatchingServices(hidMatchingDirectory, ioiterator);
    if (ioMatchingServiceReturn != IOReturn.kIOReturnSuccess || ioiterator == MemorySegment.NULL) {
      log.log(Level.SEVERE, "Failed to create HID iterator: " + IOReturn.toString(ioMatchingServiceReturn));
      return;
    }

    MemorySegment hidDevice;
    while ((hidDevice = MacOS.IOIteratorNext(ioiterator)) != MemorySegment.NULL) {
      try {
        var pluginInterfaceSegment = this.memoryArena.allocate(IOCFPlugInInterface.$LAYOUT);
        var scoreSegment = this.memoryArena.allocate(JAVA_LONG);
        var pluginInterfaceReturn = MacOS.IOCreatePlugInInterfaceForService(hidDevice, pluginInterfaceSegment, scoreSegment);
        if (pluginInterfaceReturn != IOReturn.kIOReturnSuccess) {
          log.log(Level.SEVERE, "Failed to create plugin interface: " + IOReturn.toString(pluginInterfaceReturn));
          continue;
        }

        var pluginInterface = IOCFPlugInInterface.read(pluginInterfaceSegment);
        var deviceInterfaceSegment = this.memoryArena.allocate(IOHIDDeviceInterface.$LAYOUT);

        var pluginReturn = pluginInterface.QueryInterface(MacOS.kIOHIDDeviceInterfaceID, deviceInterfaceSegment);
        if (pluginReturn != IOReturn.kIOReturnSuccess || deviceInterfaceSegment == MemorySegment.NULL) {
          log.log(Level.SEVERE, "Failed to query HID device interface: " + IOReturn.toString(pluginReturn));
          continue;
        }

        pluginInterface.release();

        var deviceInterface = IOHIDDeviceInterface.read(deviceInterfaceSegment);
        deviceInterface.hidDevice = hidDevice;
      } catch (Throwable e) {
        log.log(Level.SEVERE, "Failed to query HID device interface: " + e.getMessage());
        MacOS.IOObjectRelease(hidDevice);
        continue;
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
