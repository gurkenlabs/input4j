package de.gurkenlabs.input4j.foreign.macos.iokit;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;

import java.awt.*;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public class IOKitPlugin extends AbstractInputDevicePlugin {
  private static final Logger log = Logger.getLogger(IOKitPlugin.class.getName());

  private final Collection<IOHIDDevice> devices = ConcurrentHashMap.newKeySet();

  @Override
  public void internalInitDevices(Frame owner) {
    new Thread(() -> {
      final Arena memoryArena = Arena.ofConfined();
      try {
        var ioHIDManager = MacOS.initHIDManager(hidInputValueCallbackPointer(memoryArena));
        var ioHIDDevices = MacOS.getSupportedHIDDevices(memoryArena, ioHIDManager);

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

        // Start the event loop in a separate thread
        MacOS.runEventLoop(memoryArena, ioHIDManager);
      } catch (Throwable e) {
        log.log(Level.SEVERE, "Failed to initialize IOKit devices", e);
      } finally {
        memoryArena.close();
      }
    }).start();
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

      // TODO: creating a service plugin interface for this service type is not supported
      //      var io_service_t = MacOS.IOHIDDeviceGetService(ioHIDDevice);
      //      var pluginInterface = this.memoryArena.allocate(JAVA_LONG);
      //      var score = this.memoryArena.allocate(JAVA_INT);
      // var createInterfaceResult = MacOS.IOCreatePlugInInterfaceForService(memoryArena, io_service_t, pluginInterface, score);
      // if(createInterfaceResult != IOReturn.kIOReturnSuccess) {
      //  log.log(Level.WARNING, "Failed to create plugin interface for service " + io_service_t + " with error " + IOReturn.toString(createInterfaceResult));
      //  continue;
      //}

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
    // TODO: Clean up devices and elements
    //   use IOHIDManagerClose instead of manually releasing the object
  }

  public void hidInputValueCallback(MemorySegment context, int result, MemorySegment sender, MemorySegment ioHIDValueRef) {
    var element = MacOS.IOHIDValueGetElement(ioHIDValueRef);
    var value = MacOS.IOHIDValueGetIntegerValue(ioHIDValueRef);
    var timestamp = MacOS.IOHIDValueGetTimeStamp(ioHIDValueRef);
    System.out.println("Element: " + element.address() + ", Value: " + value + ", Timestamp: " + timestamp);
  }

  private MemorySegment hidInputValueCallbackPointer(Arena memoryArena) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle enumDeviceMethodHandle = MethodHandles.lookup()
            .bind(this, "hidInputValueCallback", MethodType.methodType(void.class, MemorySegment.class, int.class, MemorySegment.class, MemorySegment.class));

    return Linker.nativeLinker().upcallStub(
            enumDeviceMethodHandle, FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS, ADDRESS), memoryArena);
  }
}
