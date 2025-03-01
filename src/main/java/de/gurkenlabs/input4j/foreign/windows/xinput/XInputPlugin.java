package de.gurkenlabs.input4j.foreign.windows.xinput;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.components.XInput;
import de.gurkenlabs.input4j.foreign.NativeHelper;

import java.awt.*;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * The {@code XInputPlugin} class is responsible for managing XInput devices.
 * It initializes, polls, and handles rumble functionality for XInput devices.
 */
public final class XInputPlugin extends AbstractInputDevicePlugin {
  private static final int MAX_XINPUT_DEVICES = 4;
  private static final MethodHandle xInputGetState;
  private static final MethodHandle xInputSetState;
  private static final MethodHandle xInputGetCapabilities;

  private final Arena memoryArena = Arena.ofConfined();
  private final MemorySegment stateSegment = memoryArena.allocate(XINPUT_STATE.$LAYOUT);

  private final Map<String, XINPUT_GAMEPAD> nativeDevices = new ConcurrentHashMap<>();

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
    this.setDevices(refreshInputDevices());
  }

  /**
   * Closes the plugin and clears the collection of devices.
   */
  @Override
  public void close() {
    super.close();

    this.nativeDevices.clear();
    this.memoryArena.close();
  }

  @Override
  protected Collection<InputDevice> refreshInputDevices() {
    // Initialize XInput devices
    var inputDevices = new ArrayList<InputDevice>();
    for (int i = 0; i < MAX_XINPUT_DEVICES; i++) {
      var deviceId = Integer.toString(i);
      var state = getState(i);
      if (state == null) {
        this.nativeDevices.remove(deviceId);
        continue;
      }

      if (this.nativeDevices.containsKey(deviceId)) {
        var existingDevice = this.getAll().stream().filter(device -> device.getID().equals(deviceId)).findFirst();
        if (existingDevice.isPresent()) {
          inputDevices.add(existingDevice.get());
          continue;
        }
      }

      state.Gamepad.userIndex = i;
      var inputDevice = initInputDevice(state.Gamepad);
      inputDevices.add(inputDevice);
      this.nativeDevices.put(inputDevice.getID(), state.Gamepad);
    }

    return inputDevices;
  }

  /**
   * Normalizes the trigger value to a float between 0.0 and 1.0, considering a specified threshold.
   *
   * @param triggerValue The raw trigger value (0 to 255).
   * @return The normalized trigger value as a float between 0.0 and 1.0.
   */
  static float normalizeTrigger(byte triggerValue) {
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
  static float normalizeSignedShort(short shortValue, int deadzone) {
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

  private InputDevice initInputDevice(XINPUT_GAMEPAD gamepad) {
    var type = "XInput Device";
    var capabilities = getCapabilities(gamepad.userIndex);
    if (capabilities != null) {
      type = capabilities.getTypeName();
    }

    // Prepare components list based on the gamepad fields and XInputButton
    var components = new ArrayList<InputComponent>();

    var instanceName = type + " (" + gamepad.userIndex + ")";
    var device = new InputDevice(Integer.toString(gamepad.userIndex), instanceName, null, this::pollXInputDevice, this::rumbleXInputDevice);

    // order is important here, as the order of the components is used to map the polled data
    components.add(new InputComponent(device, XInput.DPAD_UP));
    components.add(new InputComponent(device, XInput.DPAD_DOWN));
    components.add(new InputComponent(device, XInput.DPAD_LEFT));
    components.add(new InputComponent(device, XInput.DPAD_RIGHT));
    components.add(new InputComponent(device, XInput.START));
    components.add(new InputComponent(device, XInput.BACK));
    components.add(new InputComponent(device, XInput.LEFT_THUMB));
    components.add(new InputComponent(device, XInput.RIGHT_THUMB));
    components.add(new InputComponent(device, XInput.LEFT_SHOULDER));
    components.add(new InputComponent(device, XInput.RIGHT_SHOULDER));
    components.add(new InputComponent(device, XInput.A));
    components.add(new InputComponent(device, XInput.B));
    components.add(new InputComponent(device, XInput.X));
    components.add(new InputComponent(device, XInput.Y));

    components.add(new InputComponent(device, XInput.LEFT_TRIGGER));
    components.add(new InputComponent(device, XInput.RIGHT_TRIGGER));
    components.add(new InputComponent(device, XInput.LEFT_THUMB_X));
    components.add(new InputComponent(device, XInput.LEFT_THUMB_Y));
    components.add(new InputComponent(device, XInput.RIGHT_THUMB_X));
    components.add(new InputComponent(device, XInput.RIGHT_THUMB_Y));

    device.setComponents(components);

    log.log(Level.FINE, "Found XInput device: " + gamepad.userIndex);
    return device;
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
    setVibration(Integer.parseInt(inputDevice.getName()),
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
    this.refreshDevices();
    var polledValues = new float[inputDevice.getComponents().size()];

    var deviceId = Integer.parseInt(inputDevice.getID());
    var state = getState(deviceId);
    if (state == null) {
      return new float[0];
    }

    int i = 0;
    for (; i < XInputButton.values.length; i++) {
      var button = XInputButton.values[i];
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

  private XINPUT_CAPABILITIES getCapabilities(int userIndex) {
    try {
      var capabilitiesSegment = this.memoryArena.allocate(XINPUT_CAPABILITIES.$LAYOUT);

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
  private synchronized XINPUT_STATE getState(int userIndex) {
    try {
      int result = (int) xInputGetState.invoke(userIndex, this.stateSegment);
      if (result == Result.ERROR_SUCCESS) {
        return XINPUT_STATE.read(this.stateSegment);
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
    try {
      var vibration = new XINPUT_VIBRATION();
      vibration.wLeftMotorSpeed = wLeftMotorSpeed;
      vibration.wRightMotorSpeed = wRightMotorSpeed;

      var segment = this.memoryArena.allocate(XINPUT_VIBRATION.$LAYOUT);
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
