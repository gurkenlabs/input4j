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

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * Plugin for managing IOHID devices on macOS.
 * This class handles the initialization, polling, and closing of IOHID devices.
 *
 * <p>IOHID (Input/Output Human Interface Device) is a framework in macOS that provides
 * support for communication with human interface devices such as keyboards, mice, game controllers,
 * and other input devices. This plugin leverages the IOHID framework to manage these devices.</p>
 *
 * <p>The class performs the following key functions:</p>
 * <ul>
 *   <li><b>Initialization:</b> Initializes the HID manager and retrieves the list of supported HID devices.</li>
 *   <li><b>Polling:</b> Polls the HID devices to read their current state and input values.</li>
 *   <li><b>Closing:</b> Properly closes the HID manager and releases resources when the plugin is no longer needed.</li>
 * </ul>
 */
public class IOKitPlugin extends AbstractInputDevicePlugin {
  private final Collection<IOHIDDevice> devices = ConcurrentHashMap.newKeySet();
  private Thread eventLoopThread;

  private boolean devicesInitialized;

  @Override
  public void internalInitDevices(Frame owner) {
    eventLoopThread = new Thread(() -> {
      MemorySegment ioHIDManager = MemorySegment.NULL;
      try (Arena memoryArena = Arena.ofConfined()) {
        log.log(Level.FINE, "Initializing HID manager...");
        ioHIDManager = MacOS.initHIDManager(hidInputValueCallbackPointer(memoryArena));
        var ioHIDDevices = MacOS.getSupportedHIDDevices(memoryArena, ioHIDManager);

        for (var ioHIDDevice : ioHIDDevices) {
          log.log(Level.FINE, "Found HID device: " + ioHIDDevice.productName);
          var inputDevice = new InputDevice(ioHIDDevice.productName, ioHIDDevice.manufacturer + " (" + ioHIDDevice.transport + ")", this::pollIOHIDDevice, null);
          ioHIDDevice.inputDevice = inputDevice;

          for (var element : ioHIDDevice.getElements()) {
            if (element.getUsage() == IOHIDElementUsage.UNDEFINED) {
              continue;
            }

            var component = new InputComponent(inputDevice, element.getIdentifier(), element.getName());
            inputDevice.addComponent(component);
          }

          IOKitVirtualComponentHandler.prepareVirtualComponents(inputDevice, inputDevice.getComponents());
          devices.add(ioHIDDevice);
        }

        devicesInitialized = true;
        if (!devices.isEmpty()) {
          log.log(Level.FINE, "Starting event loop for HID manager");
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

    // Wait for devices to be initialized or timeout after 3 seconds to prevent blocking the main thread
    int waited = 0;
    while (waited < 3000 && !devicesInitialized) {
      try {
        Thread.sleep(100);
        waited += 100;
      } catch (InterruptedException e) {
        log.log(Level.SEVERE, "Initialization interrupted", e);
        throw new RuntimeException(e);
      }
    }

    if (devicesInitialized) {
      log.log(Level.FINE, "Devices initialized successfully");
    } else {
      log.log(Level.WARNING, "Device initialization timed out");
    }
  }

  private float[] pollIOHIDDevice(InputDevice inputDevice) {
    log.log(Level.FINE, "Polling IOHIDDevice for input device: " + inputDevice.getInstanceName());
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
        log.log(Level.FINE, "Native element not found for component ID: " + component.getId());
        continue;
      }

      var elementValue = element.currentValue;
      log.log(Level.FINEST, "Element value for component ID " + component.getId() + ": " + elementValue);
      var value = normalizeInputValue(elementValue, element, component.isAxis());
      values[i] = value;
    }

    return IOKitVirtualComponentHandler.handlePolledValues(inputDevice, values);
  }

  static float normalizeInputValue(int elementValue, IOHIDElement element, boolean isAxis) {
    float value = elementValue;
    if (isAxis && element.usage != IOHIDElementUsage.HAT_SWITCH) {
      int midpoint = Math.round(element.min + element.max / 2.0f);
      if (value == 0 || Math.abs(elementValue - midpoint) <= 2) {
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

    this.devices.clear();
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
