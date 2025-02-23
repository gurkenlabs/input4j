package de.gurkenlabs.input4j.foreign.macos.iokit;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;

import java.awt.*;
import java.lang.foreign.Arena;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class IOKitPlugin extends AbstractInputDevicePlugin {
  private static final Logger log = Logger.getLogger(IOKitPlugin.class.getName());

  private final Arena memoryArena = Arena.ofConfined();
  private final Collection<IOHIDDevice> devices = ConcurrentHashMap.newKeySet();

  @Override
  public void internalInitDevices(Frame owner) {
    var ioHIDDevices = MacOS.getSupportedHIDDevices(memoryArena);

    for (var ioHIDDevice : ioHIDDevices) {
      var inputDevice = new InputDevice(ioHIDDevice.productName, ioHIDDevice.manufacturer + " (" + ioHIDDevice.transport + ")", this::pollIOHIDDevice, null);
      ioHIDDevice.inputDevice = inputDevice;

      for (var element : ioHIDDevice.getElements()) {
        if (element.getUsage() == IOHIDElementUsage.UNDEFINED) {
          continue;
        }

        var component = new InputComponent(inputDevice, element.getIdentifier(), element.getName());
        inputDevice.addComponent(component);
      }
      devices.add(ioHIDDevice);
    }
  }

  private float[] pollIOHIDDevice(InputDevice inputDevice) {
    var values = new float[inputDevice.getComponents().size()];

    // find native IOHIDDevice and poll elements
    var ioHIDDevice = this.devices.stream().filter(x -> x.inputDevice.equals(inputDevice)).findFirst().orElse(null);
    if (ioHIDDevice == null) {
      log.log(Level.WARNING, "IOHIDDevice not found for input device " + inputDevice.getInstanceName());
      return values;
    }

    for (int i = 0; i < inputDevice.getComponents().size(); i++) {
      var component = inputDevice.getComponents().get(i);
      var element = ioHIDDevice.getElements().stream().filter(x -> x.getIdentifier() == component.getId()).findFirst().orElse(null);
      if (element == null) {
        log.log(Level.WARNING, "IOHIDElement not found for component " + component.getId());
        continue;
      }

      var ioHIDValueRefSegment = this.memoryArena.allocate(JAVA_LONG);
      var getValueResult = MacOS.IOHIDDeviceGetValue(ioHIDDevice, element, ioHIDValueRefSegment);
      if (getValueResult != IOReturn.kIOReturnSuccess) {
        log.log(Level.WARNING, "Failed to get value for element " + element + " with error " + IOReturn.toString(getValueResult));
        continue;
      }

      var io_service_t = MacOS.IOHIDDeviceGetService(ioHIDDevice);
      var pluginInterface = this.memoryArena.allocate(JAVA_LONG);
      var score = this.memoryArena.allocate(JAVA_INT);

      var serviceClass = MacOS.IORegistryEntryGetClass(memoryArena, io_service_t);
      var createInterfaceResult = MacOS.IOCreatePlugInInterfaceForService(memoryArena, io_service_t, pluginInterface, score);
      if(createInterfaceResult != IOReturn.kIOReturnSuccess) {
        log.log(Level.WARNING, "Failed to create plugin interface for service " + io_service_t + " with error " + IOReturn.toString(createInterfaceResult));
        continue;
      }

      // TODO: This throws.
      // Do I need to iterate the elements every time upon polling and cannot hold the addresses?
      // Another approach: Use the event based API instead of polling that provides the IOHIDValueRef directly
      // var value = ioHIDValueRef.integerValue;
      // var timestamp = MacOS.IOHIDValueGetTimeStamp(ioHIDValueRefSegment);
      values[i] = 0;
    }

    return values;
  }

  @Override
  public Collection<InputDevice> getAll() {
    return this.devices.stream().map(x -> x.inputDevice).toList();
  }

  @Override
  public void close() {
    memoryArena.close();

    // TODO: Clean up devices and elements
  }
}
