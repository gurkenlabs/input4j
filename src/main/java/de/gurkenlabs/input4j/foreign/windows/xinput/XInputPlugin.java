package de.gurkenlabs.input4j.foreign.windows.xinput;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
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
  private static final MethodHandle xInputGetCapabilities;

  private final Collection<XINPUT_GAMEPAD> devices = ConcurrentHashMap.newKeySet();

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

    xInputGetCapabilities = NativeHelper.downcallHandle(
            "XInputGetCapabilities",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
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

        state.Gamepad.userIndex = i;

        var type = "XInput Device";
        var capabilities = getCapabilities(i);
        if (capabilities != null) {
          type = capabilities.getTypeName();
        }

        // Prepare components list based on the gamepad fields and XInputButton
        var components = new ArrayList<InputComponent>();

        var instanceName = type + " (" + state.Gamepad.userIndex + ")";
        var device = new InputDevice(instanceName, null, this::pollXInputDevice, this::rumbleXInputDevice);

        // order is important here, as the order of the components is used to map the polled data
        components.add(new InputComponent(device, InputComponent.XInput.DPAD_UP));
        components.add(new InputComponent(device, InputComponent.XInput.DPAD_DOWN));
        components.add(new InputComponent(device, InputComponent.XInput.DPAD_LEFT));
        components.add(new InputComponent(device, InputComponent.XInput.DPAD_RIGHT));
        components.add(new InputComponent(device, InputComponent.XInput.START));
        components.add(new InputComponent(device, InputComponent.XInput.BACK));
        components.add(new InputComponent(device, InputComponent.XInput.LEFT_THUMB));
        components.add(new InputComponent(device, InputComponent.XInput.RIGHT_THUMB));
        components.add(new InputComponent(device, InputComponent.XInput.LEFT_SHOULDER));
        components.add(new InputComponent(device, InputComponent.XInput.RIGHT_SHOULDER));
        components.add(new InputComponent(device, InputComponent.XInput.A));
        components.add(new InputComponent(device, InputComponent.XInput.B));
        components.add(new InputComponent(device, InputComponent.XInput.X));
        components.add(new InputComponent(device, InputComponent.XInput.Y));

        components.add(new InputComponent(device, InputComponent.XInput.LEFT_TRIGGER));
        components.add(new InputComponent(device, InputComponent.XInput.RIGHT_TRIGGER));
        components.add(new InputComponent(device, InputComponent.XInput.LEFT_THUMB_X));
        components.add(new InputComponent(device, InputComponent.XInput.LEFT_THUMB_Y));
        components.add(new InputComponent(device, InputComponent.XInput.RIGHT_THUMB_X));
        components.add(new InputComponent(device, InputComponent.XInput.RIGHT_THUMB_Y));

        // Add a special DPAD axis component that combines the values of all DPAD buttons
        // this is usally not provided by XInput but more consistent with other input libraries
        components.add(new InputComponent(device, InputComponent.Axis.DPAD));
        device.setComponents(components);

        state.Gamepad.inputDevice = device;
        devices.add(state.Gamepad);
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
    var polledValues = new float[inputDevice.getComponents().size()];

    // find native XINPUT_GAMEPAD and poll it
    var xinputGamepad = this.devices.stream().filter(x -> x.inputDevice.equals(inputDevice)).findFirst().orElse(null);
    if (xinputGamepad == null) {
      log.log(Level.WARNING, "DirectInput device not found for input device " + inputDevice.getInstanceName());
      return polledValues;
    }

    var state = getState(xinputGamepad.userIndex);
    if (state == null) {
      return new float[0];
    }

    int i = 0;
    for (; i < XInputButton.values().length; i++) {
      var button = XInputButton.values()[i];
      polledValues[i] = button.isPressed(state.Gamepad.wButtons) ? 1 : 0;
    }

    polledValues[i++] = normalizeTrigger(state.Gamepad.bLeftTrigger);
    polledValues[i++] = normalizeTrigger(state.Gamepad.bRightTrigger);
    polledValues[i++] = normalizeSignedShort(state.Gamepad.sThumbLX, XINPUT_GAMEPAD.XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE);
    polledValues[i++] = normalizeSignedShort(state.Gamepad.sThumbLY, XINPUT_GAMEPAD.XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE);
    polledValues[i++] = normalizeSignedShort(state.Gamepad.sThumbRX, XINPUT_GAMEPAD.XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE);
    polledValues[i] = normalizeSignedShort(state.Gamepad.sThumbRY, XINPUT_GAMEPAD.XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE);

    return polledValues;
  }

  /**
   * Normalizes the trigger value to a float between 0.0 and 1.0, considering a specified threshold.
   *
   * @param triggerValue The raw trigger value (0 to 255).
   * @return The normalized trigger value as a float between 0.0 and 1.0.
   */
  private static float normalizeTrigger(byte triggerValue) {
    if (Byte.toUnsignedInt(triggerValue) < XINPUT_GAMEPAD.XINPUT_GAMEPAD_TRIGGER_THRESHOLD) {
      return 0.0f;
    }
    return Byte.toUnsignedInt(triggerValue) / 255f;
  }

  /**
   * Normalizes a signed short value to a float between -1.0 and 1.0.
   *
   * @param shortValue The signed short value.
   * @param deadzone   The deadzone values are used to filter out small movements of the thumbsticks that are within a certain threshold.
   *                   This helps to avoid unintentional movements due to slight pressure or drift.
   * @return The normalized float value.
   */
  private static float normalizeSignedShort(short shortValue, int deadzone) {
    if (Math.abs(shortValue) < deadzone) {
      return 0.0f;
    }

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
    return this.devices.stream().map(x -> x.inputDevice).toList();
  }

  /**
   * Closes the plugin and clears the collection of devices.
   */
  @Override
  public void close() {
    for (var device : this.devices) {
      device.inputDevice.close();
    }

    this.devices.clear();
  }

  public static XINPUT_CAPABILITIES getCapabilities(int userIndex) {
    try (var memorySession = Arena.ofConfined()) {
      var capabilitiesSegment = memorySession.allocate(XINPUT_CAPABILITIES.$LAYOUT);

      int result = (int) xInputGetCapabilities.invoke(userIndex, 0, capabilitiesSegment);
      if (result == Result.ERROR_SUCCESS) {
        return XINPUT_CAPABILITIES.read(capabilitiesSegment);
      } else if (result == Result.ERROR_DEVICE_NOT_CONNECTED) {
        return null;
      } else {
        log.log(Level.WARNING, "XInputGetCapabilities failed for userIndex " + userIndex + " with error result " + Result.toString(result));
        return null;
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
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
