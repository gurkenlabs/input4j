package de.gurkenlabs.input4j.foreign.windows.xinput;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.foreign.NativeHelper;

import java.awt.*;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code XInputPlugin} class is responsible for managing XInput devices.
 * It initializes, polls, and handles rumble functionality for XInput devices.
 */
public final class XInputPlugin extends AbstractInputDevicePlugin {
  private static final Logger log = Logger.getLogger(XInputPlugin.class.getName());

  private static final MethodHandle xInputGetState;
  private static final MethodHandle xInputSetState;

  private Collection<InputDevice> devices = ConcurrentHashMap.newKeySet();

  static {
    System.loadLibrary("XInput1_4");

    xInputGetState = NativeHelper.downcallHandle(
            "XInputGetState",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );

    xInputSetState = NativeHelper.downcallHandle(
            "XInputSetState",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
  }

  /**
   * Initializes the XInput devices and adds them to the collection of devices.
   *
   * @param owner The frame owner.
   */
  @Override
  public void internalInitDevices(Frame owner) {
    try {
      // Initialize XInput devices
      for (int i = 0; i < 4; i++) {
        var state = getState(i);
        if (state == null) {
          continue;
        }

        // Prepare components list based on the gamepad fields and XInputButton
        var components = new ArrayList<InputComponent>();
        var device = new InputDevice(UUID.randomUUID(), null, Integer.toString(i), "XInput Device", this::pollXInputDevice, this::rumbleXInputDevice);

        // order is important here, as the order of the components is used to map the polled data
        for (XInputButton button : XInputButton.values()) {
          components.add(new InputComponent(device, ComponentType.Button, button.name(), false));
        }

        components.add(new InputComponent(device, ComponentType.ZAxis, "Left Trigger", false));
        components.add(new InputComponent(device, ComponentType.ZAxis, "Right Trigger", false));
        components.add(new InputComponent(device, ComponentType.XAxis, "Left Thumb X", false));
        components.add(new InputComponent(device, ComponentType.YAxis, "Left Thumb Y", false));
        components.add(new InputComponent(device, ComponentType.XAxis, "Right Thumb X", false));
        components.add(new InputComponent(device, ComponentType.YAxis, "Right Thumb Y", false));
        device.addComponents(components);

        devices.add(device);
        log.log(Level.INFO, "Found XInput device: " + i);
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Sets the vibration intensity for the specified input device.
   *
   * @param inputDevice The input device.
   * @param intensity   The vibration intensity for the left and right motors.
   */
  private void rumbleXInputDevice(InputDevice inputDevice, float[] intensity) {
    var motorSpeedLeft = 0f;
    var motorSpeedRight = 0f;
    if (intensity != null && intensity.length > 0) {
      motorSpeedLeft = Math.clamp(intensity[0], 0, 1);
      motorSpeedRight = intensity.length > 1 ? Math.clamp(intensity[2], 0, 1) : motorSpeedLeft;
    }

    // Set the vibration for each motor (example for two motors)
    setVibration(Integer.parseInt(inputDevice.getInstanceName()),
            (short) (motorSpeedLeft * XINPUT_VIBRATION.MAX_VIBRATION),
            (short) (motorSpeedRight * XINPUT_VIBRATION.MAX_VIBRATION));
  }

  /**
   * Polls the input device state and returns the current state of its components.
   *
   * @param inputDevice The input device.
   * @return The current state of the input device's components.
   */
  private float[] pollXInputDevice(InputDevice inputDevice) {
    var userIndex = Integer.parseInt(inputDevice.getInstanceName());

    var state = getState(userIndex);
    if (state == null) {
      return new float[0];
    }

    var polledData = new float[inputDevice.getComponents().size()];

    int i = 0;
    for (; i < XInputButton.values().length; i++) {
      var button = XInputButton.values()[i];
      polledData[i] = button.isPressed(state.Gamepad.wButtons) ? 1 : 0;
    }

    polledData[i++] = Byte.toUnsignedInt(state.Gamepad.bLeftTrigger) / 255f;
    polledData[i++] = Byte.toUnsignedInt(state.Gamepad.bRightTrigger) / 255f;
    polledData[i++] = normalizeSignedShort(state.Gamepad.sThumbLX);
    polledData[i++] = normalizeSignedShort(state.Gamepad.sThumbLY);
    polledData[i++] = normalizeSignedShort(state.Gamepad.sThumbRX);
    polledData[i] = normalizeSignedShort(state.Gamepad.sThumbRY);

    return polledData;
  }

  /**
   * Normalizes a signed short value to a float between -1.0 and 1.0.
   *
   * @param shortValue The signed short value.
   * @return The normalized float value.
   */
  private static float normalizeSignedShort(short shortValue) {
    if (shortValue == Short.MIN_VALUE) {
      return -1.0f;
    }
    if (shortValue == Short.MAX_VALUE) {
      return 1.0f;
    }
    return (float) shortValue / Short.MAX_VALUE;
  }

  /**
   * Returns the collection of all XInput devices.
   *
   * @return The collection of all XInput devices.
   */
  @Override
  public Collection<InputDevice> getAll() {
    return this.devices;
  }

  /**
   * Closes the plugin and clears the collection of devices.
   */
  @Override
  public void close() {
    for(var device : this.devices) {
      device.close();
    }
    this.devices.clear();
  }

  /**
   * Gets the state of the XInput device for the specified user index.
   *
   * @param userIndex The user index.
   * @return The state of the XInput device, or {@code null} if the device is not connected.
   */
  private XINPUT_STATE getState(int userIndex) {
    try (var memorySession = Arena.ofConfined()) {
      var segment = memorySession.allocate(XINPUT_STATE.$LAYOUT);

      int result = (int) xInputGetState.invoke(userIndex, segment);
      if (result == Result.ERROR_SUCCESS) {
        return XINPUT_STATE.read(segment);
      } else if (result == Result.ERROR_DEVICE_NOT_CONNECTED) {
        return null;
      } else {
        log.log(Level.WARNING, "XInputGetState failed for userIndex " + userIndex + " with error result " + Result.toString(result));
        return null;
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  /**
   * Sets the vibration for the XInput device for the specified user index.
   *
   * @param userIndex        The user index.
   * @param wLeftMotorSpeed  The speed of the left motor.
   * @param wRightMotorSpeed The speed of the right motor.
   */
  private void setVibration(int userIndex, short wLeftMotorSpeed, short wRightMotorSpeed) {
    try (var memorySession = Arena.ofConfined()) {
      var vibration = new XINPUT_VIBRATION();
      vibration.wLeftMotorSpeed = wLeftMotorSpeed;
      vibration.wRightMotorSpeed = wRightMotorSpeed;

      var segment = memorySession.allocate(XINPUT_VIBRATION.$LAYOUT);
      vibration.write(segment);
      int result = (int) xInputSetState.invoke(userIndex, segment);
      if (result != Result.ERROR_SUCCESS) {
        log.log(Level.WARNING, "XInputSetState failed for userIndex " + userIndex + " with result " + Result.toString(result));
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
