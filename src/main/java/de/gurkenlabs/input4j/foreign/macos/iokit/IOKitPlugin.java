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
  private Thread eventLoopThread;

  private boolean devicesInitialized;

  @Override
  public void internalInitDevices(Frame owner) {
    eventLoopThread = new Thread(() -> {
      MemorySegment ioHIDManager = MemorySegment.NULL;
      try (Arena memoryArena = Arena.ofConfined()) {
        ioHIDManager = MacOS.initHIDManager(hidInputValueCallbackPointer(memoryArena));
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

        devicesInitialized = true;
        if (!devices.isEmpty()) {
          // Start the event loop in a separate thread
          MacOS.runEventLoop(memoryArena, ioHIDManager);
        }
      } catch (Throwable e) {
        log.log(Level.SEVERE, "Failed to initialize IOKit devices", e);
      } finally {
        if (!ioHIDManager.equals(MemorySegment.NULL)) {
          var closeReturn = MacOS.IOHIDManagerClose(ioHIDManager);
          if (closeReturn != IOReturn.kIOReturnSuccess) {
            log.log(Level.WARNING, "Failed to close IOHIDManager with error " + IOReturn.toString(closeReturn));
          }
        }
      }
    });

    eventLoopThread.start();

    int waited = 0;
    while (waited < 3000 && !devicesInitialized) {
      try {
        Thread.sleep(100);
        waited += 100;
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
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

      var elementValue = element.currentValue;

      var value = normalizeInputValue(elementValue, element);
      values[i] = value;
    }

    return values;
  }

  static float normalizeInputValue(int elementValue, IOHIDElement element) {
    float value = elementValue;
    if (element.type == IOHIDElementType.AXIS) {
      int midpoint = Math.round(element.min + element.max / 2.0f);
      if (Math.abs(elementValue - midpoint) <= 2) {
        value = 0;
      } else {
        // Ensure value is within the range [min, max]
        // Then normalize the value to the range [-1, 1]
        value = Math.max(element.min, Math.min(element.max, value));
        value = (value - element.min) / (float) (element.max - element.min) * 2 - 1;
      }
    }

    if (element.type == IOHIDElementType.BUTTON) {
      value = value == 0 ? 0 : 1;
    }

    return value;
  }

  @Override
  public Collection<InputDevice> getAll() {
    return this.devices.stream().map(x -> x.inputDevice).toList();
  }

  @Override
  public void close() {
    if (eventLoopThread != null) {
      eventLoopThread.interrupt();
    }
  }

  /**
   * This is called out of native code when an IOHIDElement  provides a value.
   */
  public void hidInputValueCallback(MemorySegment context, int result, MemorySegment sender, MemorySegment ioHIDValueRef) {
    var element = MacOS.IOHIDValueGetElement(ioHIDValueRef);
    var value = MacOS.IOHIDValueGetIntegerValue(ioHIDValueRef);
    var timestamp = MacOS.IOHIDValueGetTimeStamp(ioHIDValueRef);
    // find element from the list in this instance according to the address of the element
    var ioHIDElement = this.devices.stream().flatMap(x -> x.getElements().stream()).filter(x -> x.address == element.address()).findFirst().orElse(null);
    if (ioHIDElement == null) {
      log.log(Level.WARNING, "IOHIDElement not found for address " + element.address());
      return;
    }

    ioHIDElement.currentValue = value;
  }

  private MemorySegment hidInputValueCallbackPointer(Arena memoryArena) throws Throwable {
    // Create a method handle to the Java function as a callback
    MethodHandle enumDeviceMethodHandle = MethodHandles.lookup()
            .bind(this, "hidInputValueCallback", MethodType.methodType(void.class, MemorySegment.class, int.class, MemorySegment.class, MemorySegment.class));

    return Linker.nativeLinker().upcallStub(
            enumDeviceMethodHandle, FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS, ADDRESS), memoryArena);
  }
}
