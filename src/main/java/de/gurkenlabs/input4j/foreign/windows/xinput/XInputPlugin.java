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
        var device = new InputDevice(UUID.randomUUID(), null, Integer.toString(i), "XInput Device", this::pollXInputDevice);

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

  private float normalizeSignedShort(short shortValue) {
    if (shortValue == Short.MIN_VALUE) {
      return -1.0f;
    }
    if (shortValue == Short.MAX_VALUE) {
      return 1.0f;
    }
    return (float) shortValue / Short.MAX_VALUE;
  }

  @Override
  public Collection<InputDevice> getAll() {
    return this.devices;
  }

  @Override
  public void close() {
    this.devices.clear();
  }

  public XINPUT_STATE getState(int userIndex) {
    try (var memorySession = Arena.ofConfined()) {
      var segment = memorySession.allocate(XINPUT_STATE.$LAYOUT);

      int result = (int) xInputGetState.invoke(userIndex, segment);
      if (result == Result.ERROR_SUCCESS) {
        return XINPUT_STATE.read(segment);
      } else {
        log.log(Level.WARNING, "XInputGetState failed for userIndex " + userIndex + " with error result " + Result.toString(result));
        return null;
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return null;
    }
  }

  // TODO: Expose in the library API
  public void setVibration(int userIndex, short wLeftMotorSpeed, short wRightMotorSpeed) {
    try (var memorySession = Arena.ofConfined()) {
      var vibration = new XINPUT_VIBRATION();
      vibration.wLeftMotorSpeed = wLeftMotorSpeed;
      vibration.wRightMotorSpeed = wRightMotorSpeed;

      var segment = memorySession.allocate(XINPUT_VIBRATION.$LAYOUT);
      vibration.write(segment);
      int result = (int) xInputSetState.invoke(userIndex, segment);
      if (result != Result.ERROR_SUCCESS) { // ERROR_SUCCESS
        log.log(Level.WARNING, "XInputSetState failed for userIndex " + userIndex + " with result " + Result.toString(result));
      }
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }
}
