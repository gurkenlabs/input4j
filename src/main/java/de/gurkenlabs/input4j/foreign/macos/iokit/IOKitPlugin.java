package de.gurkenlabs.input4j.foreign.macos.iokit;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.BatteryInfo;
import de.gurkenlabs.input4j.BatteryLevel;
import de.gurkenlabs.input4j.BatteryType;
import de.gurkenlabs.input4j.ControllerDatabase;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
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
  private final Map<String, IOHIDDevice> nativeDevices = new ConcurrentHashMap<>();
  /**
   * Cookie-based reverse index used by {@link #hidInputValueCallback} to
   * resolve an incoming {@code IOHIDValue} to the {@link IOHIDElement} that
   * produced it. The outer key is the {@code IOHIDDeviceRef} address; the
   * inner key is the element's {@code IOHIDElementGetCookie} value. The
   * cookie is unique within a device, so this lookup is robust against any
   * ambiguity in raw {@code IOHIDElementRef} pointer comparison.
   */
  private final Map<Long, Map<Integer, IOHIDElement>> elementsByDeviceAndCookie = new ConcurrentHashMap<>();
  private Thread eventLoopThread;
  private Arena batteryArena;

  @Override
  public void internalInitDevices(Frame owner) {
    batteryArena = Arena.ofShared();
    var initLatch = new CountDownLatch(1);
    eventLoopThread = new Thread(() -> {
      MemorySegment ioHIDManager = MemorySegment.NULL;
      try (Arena memoryArena = Arena.ofConfined()) {
        log.log(Level.FINE, "Initializing HID manager...");
        ioHIDManager = MacOS.initHIDManager(hidInputValueCallbackPointer(memoryArena));
        var ioHIDDevices = MacOS.getSupportedHIDDevices(memoryArena, ioHIDManager);

        populateDevices(ioHIDDevices);
        initLatch.countDown();

        if (!nativeDevices.isEmpty()) {
          log.log(Level.FINE, "Starting event loop for HID manager");
          MacOS.runEventLoop(memoryArena, ioHIDManager);
        }
      } catch (Throwable e) {
        log.log(Level.SEVERE, "Failed to initialize IOKit devices", e);
        initLatch.countDown();
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

    try {
      if (!initLatch.await(3, TimeUnit.SECONDS)) {
        log.log(Level.WARNING, "Device initialization timed out");
      } else {
        log.log(Level.FINE, "Devices initialized successfully");
      }
    } catch (InterruptedException e) {
      log.log(Level.SEVERE, "Initialization interrupted", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Assembles {@link InputDevice} wrappers for the given discovered native
   * devices, attaches their components, runs virtual-component preparation,
   * registers the devices in {@link #nativeDevices} and publishes the
   * resulting {@link InputDevice}s via {@link #setDevices}.
   * <p>
   * Package-private for unit testing so that the FFM-bound discovery path
   * can be exercised on non-macOS hosts by passing in hand-built
   * {@link IOHIDDevice} fixtures.
   *
   * @param discoveredDevices the native devices returned by
   *                          {@link MacOS#getSupportedHIDDevices(Arena, MemorySegment)}.
   */
  void populateDevices(Collection<IOHIDDevice> discoveredDevices) {
    for (var ioHIDDevice : discoveredDevices) {
      log.log(Level.FINE, "Found HID device: " + ioHIDDevice.productName);
      String displayName = ControllerDatabase.getDisplayName(ioHIDDevice.vendorId, ioHIDDevice.productId);
      var inputDevice = new InputDevice(Long.toString(ioHIDDevice.address), ioHIDDevice.productName, ioHIDDevice.manufacturer + " (" + ioHIDDevice.transport + ")", ioHIDDevice.vendorId, ioHIDDevice.productId, displayName, this::pollIOHIDDevice, this::rumbleIOHIDDevice, this::getBatteryInfo);
      ioHIDDevice.inputDevice = inputDevice;

      var cookieIndex = new ConcurrentHashMap<Integer, IOHIDElement>();
      for (var element : ioHIDDevice.getElements()) {
        cookieIndex.put(element.cookie, element);
        if (element.getUsage() == IOHIDElementUsage.UNDEFINED) {
          continue;
        }

        var component = new InputComponent(inputDevice, element.getIdentifier(), element.getName());
        inputDevice.addComponent(component);
      }

      IOKitVirtualComponentHandler.prepareVirtualComponents(inputDevice, inputDevice.getComponents());
      nativeDevices.put(inputDevice.getID(), ioHIDDevice);
      elementsByDeviceAndCookie.put(ioHIDDevice.address, cookieIndex);
    }

    // Publish the input devices so callers of getAll() can observe them as
    // soon as we are done discovering. Must be called from the event-loop
    // thread (which is the only thread that runs populateDevices) so that
    // the writes to AbstractInputDevicePlugin.devices / .lastDeviceUpdate
    // happen-before the main thread wakes from initLatch.await().
    this.setDevices(this.nativeDevices.values().stream().map(d -> d.inputDevice).toList());
  }

  /**
   * Resolves an input value to its originating {@link IOHIDElement} using
   * the cookie-based lookup. Returns {@code null} if the device is not
   * registered or if the cookie is unknown.
   * <p>
   * Package-private for unit testing.
   */
  IOHIDElement findElement(long deviceAddress, int cookie) {
    var deviceMap = elementsByDeviceAndCookie.get(deviceAddress);
    if (deviceMap == null) {
      return null;
    }
    return deviceMap.get(cookie);
  }

  @Override
  public void close() {
    super.close();

    if (eventLoopThread != null) {
      eventLoopThread.interrupt();
    }

    this.nativeDevices.clear();
    this.elementsByDeviceAndCookie.clear();
  }

  @Override
  protected Collection<InputDevice> refreshInputDevices() {
    // TODO: implement refresh support
    return this.getAll();
  }

  static float normalizeInputValue(int elementValue, IOHIDElement element, boolean isAxis) {
    float value = elementValue;
    if (isAxis && element.usage != IOHIDElementUsage.HAT_SWITCH) {
      int midpoint = Math.round((element.min + element.max) / 2.0f);
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

  private float[] pollIOHIDDevice(InputDevice inputDevice) {
    log.log(Level.FINE, "Polling IOHIDDevice for input device: " + inputDevice.getName());
    var values = new float[inputDevice.getComponents().size()];

    // find native IOHIDDevice and poll elements
    var ioHIDDevice = this.nativeDevices.getOrDefault(inputDevice.getID(), null);
    if (ioHIDDevice == null) {
      log.log(Level.WARNING, "IOHIDDevice not found for input device " + inputDevice.getName());
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

  /**
   * This is called out of native code when an IOHIDElement  provides a value.
   */
  private void hidInputValueCallback(MemorySegment context, int result, MemorySegment sender, MemorySegment ioHIDValueRef) {
    // The sender parameter is the IOHIDDeviceRef that produced the value.
    // Together with the element's cookie (a 32-bit identifier unique within
    // a device) we can resolve the value back to the exact IOHIDElement
    // without relying on raw IOHIDElementRef pointer comparison, which can
    // be unreliable when the FFM downcall layer hands us a carrier
    // MemorySegment.
    var deviceAddress = sender.address();
    var element = MacOS.IOHIDValueGetElement(ioHIDValueRef);
    int cookie = MacOS.IOHIDElementGetCookie(element);
    int value = MacOS.IOHIDValueGetIntegerValue(ioHIDValueRef);

    var ioHIDElement = findElement(deviceAddress, cookie);
    if (ioHIDElement == null) {
      log.log(Level.FINE, "IOHIDElement not found for device " + deviceAddress + " cookie " + cookie);
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

  /** Threshold below which rumble is stopped (values 0.0-1.0). */
  private static final float RUMBLE_THRESHOLD = 0.01f;

  /**
   * Determines if the given intensity values should trigger a rumble stop (are below threshold).
   * Package-private for testing.
   *
   * @param intensity The intensity values.
   * @return true if the intensity is null, empty, or all values are below threshold.
   */
  static boolean shouldStopRumble(float[] intensity) {
    return intensity == null || intensity.length == 0
        || (intensity.length > 0 && intensity[0] < RUMBLE_THRESHOLD
            && (intensity.length == 1 || intensity[1] < RUMBLE_THRESHOLD));
  }

  /**
   * Converts intensity values (0.0-1.0) to byte values (0-255).
   * Package-private for testing.
   *
   * @param leftMotor The left motor intensity.
   * @param rightMotor The right motor intensity.
   * @return int array with [0] = leftValue, [1] = rightValue.
   */
  static int[] intensityToBytes(float leftMotor, float rightMotor) {
    float left = Math.clamp(leftMotor, 0f, 1f);
    float right = Math.clamp(rightMotor, 0f, 1f);
    return new int[] {(int) (left * 255f), (int) (right * 255f)};
  }

  /**
   * Sets the rumble (haptic feedback) intensity for the IOHID device.
   *
   * @param inputDevice The input device.
   * @param intensity  The intensity values. intensity[0] is the left/strong motor,
   *               intensity[1] (optional) is the right/weak motor.
   *               Values should be in range 0.0 to 1.0.
   */
  private void rumbleIOHIDDevice(InputDevice inputDevice, float[] intensity) {
    var ioHIDDevice = this.nativeDevices.getOrDefault(inputDevice.getID(), null);
    if (ioHIDDevice == null) {
      log.log(Level.WARNING, "IOHIDDevice not found for input device " + inputDevice.getName());
      return;
    }

    // Get the controller-specific rumble profile
    var profile = ControllerRumbleProfile.fromVendorProduct(ioHIDDevice.vendorId, ioHIDDevice.productId);

    // Check if intensity is too low to bother
    if (shouldStopRumble(intensity)) {
      // Send stop rumble (all zeros)
      sendRumbleReport(ioHIDDevice.address, profile, 0, 0);
      return;
    }

    float leftMotor = intensity[0];
    float rightMotor = intensity.length > 1 ? intensity[1] : leftMotor;

    // Convert to unsigned byte values (0-255)
    int[] motorValues = intensityToBytes(leftMotor, rightMotor);

    sendRumbleReport(ioHIDDevice.address, profile, motorValues[0], motorValues[1]);
  }

  private void sendRumbleReport(long deviceAddress, ControllerRumbleProfile profile, int leftMotor, int rightMotor) {
    try (Arena arena = Arena.ofConfined()) {
      var report = arena.allocate(JAVA_BYTE, profile.getReportSize());

      // Set the report ID at the appropriate position (typically byte 0)
      report.set(JAVA_BYTE, 0, profile.getReportId());

      // Set left and right motor values at profile-specific offsets
      // Use & 0xFF to treat as unsigned byte (0-255)
      report.set(JAVA_BYTE, profile.getLeftMotorOffset(), (byte) (leftMotor & 0xFF));
      report.set(JAVA_BYTE, profile.getRightMotorOffset(), (byte) (rightMotor & 0xFF));

      int result = MacOS.IOHIDDeviceSetReport(deviceAddress, MacOS.kIOHIDReportTypeOutput, report, profile.getReportSize());
      if (result != IOReturn.kIOReturnSuccess) {
        log.log(Level.FINE, "Failed to send rumble report with error: " + IOReturn.toString(result));
      }
    } catch (Exception e) {
      log.log(Level.WARNING, "Failed to send rumble report", e);
    }
  }

  private BatteryInfo getBatteryInfo(InputDevice inputDevice) {
    var ioHIDDevice = nativeDevices.get(inputDevice.getID());
    if (ioHIDDevice == null) {
      return null;
    }

    int batteryPercent = MacOS.getBatteryPercentage(ioHIDDevice, batteryArena);

    if (batteryPercent < 0) {
      return null;
    }

    BatteryLevel level;

    if (batteryPercent >= 75) {
      level = BatteryLevel.FULL;
    } else if (batteryPercent >= 50) {
      level = BatteryLevel.MEDIUM;
    } else if (batteryPercent >= 25) {
      level = BatteryLevel.LOW;
    } else {
      level = BatteryLevel.EMPTY;
    }

    return new BatteryInfo(BatteryType.UNKNOWN, level, false);
  }
}
