package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.components.Axis;
import de.gurkenlabs.input4j.components.Button;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinuxEventDevicePluginTests {
  @Test
  void testNormalizeInputValueForAxis() {
    // Test case 1: EV_ABS type, value within range
    LinuxEventComponent component = new LinuxEventComponent(LinuxComponentType.ABS_HAT1X, true, false, LinuxEventDevice.EV_ABS, 0x12, -1000, 1000, 0, 10);

    input_event event = new input_event();
    event.value = 500;

    float normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(0.5, normalizedValue, 0.01);

    // Test case 2: EV_ABS type, value at midpoint
    event.value = 0;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(0, normalizedValue, 0.01);

    // Test case 3: EV_ABS type, value at min
    event.value = -1000;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(-1, normalizedValue, 0.01);

    // Test case 4: EV_ABS type, value at max
    event.value = 1000;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);

    // Test case 5: fuzz does not define a deadzone
    event.value = 5;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(0.005, normalizedValue, 0.000001);

    // Test case 6: EV_ABS type, value outside min range
    event.value = -5000;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(-1, normalizedValue, 0.01);

    // Test case 7: EV_ABS type, value outside max range
    event.value = 5000;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);
  }

  @Test
  void testNormalizeInputValueForOneSidedAxis() {
    var component = new LinuxEventComponent(LinuxComponentType.ABS_Z, true, false,
        LinuxEventDevice.EV_ABS, LinuxEventCode.ABS_Z, 0, 255, 0, 10);
    var event = new input_event();

    event.value = 0;
    assertEquals(0f, LinuxEventDevicePlugin.normalizeInputValue(event, component));

    event.value = 255;
    assertEquals(1f, LinuxEventDevicePlugin.normalizeInputValue(event, component));
  }

  @Test
  void testNormalizeInputValueForEVKey() {
    // Test case: EV_KEY type, value is 0
    LinuxEventComponent component = new LinuxEventComponent(LinuxEventDevice.EV_KEY, 0);
    input_event event = new input_event();
    event.value = 0;

    float normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(0, normalizedValue, 0.01);

    // Test case: EV_KEY type, value is non-zero
    event.value = 1;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);

    // Test case: EV_KEY type, value is greater than 1
    event.value = 100;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);

    // Test case: EV_KEY type, value is smaller than -1
    event.value = -100;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);

    // Test case: EV_KEY type, value is negative
    event.value = -1;
    normalizedValue = LinuxEventDevicePlugin.normalizeInputValue(event, component);
    assertEquals(1, normalizedValue, 0.01);
  }

  @Test
  void testGetComponentIndexByNativeId() {
    // Create a mock InputDevice with components
    InputDevice inputDevice = new InputDevice("123", "Test Device", "Test Device", null, null);
    InputComponent buttonComponent = new InputComponent(inputDevice, new InputComponent.ID(Button.BUTTON_1, 123), "Button1", false);
    InputComponent axisComponent = new InputComponent(inputDevice, new InputComponent.ID(Axis.AXIS_X, 456), "Axis1", false);
    inputDevice.addComponent(buttonComponent);
    inputDevice.addComponent(axisComponent);

    // Test case 1: EV_KEY type, matching component
    input_event event = new input_event();
    event.type = LinuxEventDevice.EV_KEY;
    event.code = 123;
    int index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(0, index);

    // Test case 2: EV_ABS type, matching component
    event.type = LinuxEventDevice.EV_ABS;
    event.code = 456;
    index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(1, index);

    // Test case 3: EV_KEY type, no matching component
    event.type = LinuxEventDevice.EV_KEY;
    event.code = 3;
    index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(Linux.ERROR, index);

    // Test case 4: EV_ABS type, no matching component
    event.type = LinuxEventDevice.EV_ABS;
    event.code = 3;
    index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(Linux.ERROR, index);

    // Test case 5: Unknown component type
    InputComponent unknownComponent = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.UNKNOWN, 1111, "Unknown"), "Unknown", false);
    inputDevice.addComponent(unknownComponent);
    event.type = LinuxEventDevice.EV_KEY;
    event.code = 4;
    index = LinuxEventDevicePlugin.getComponentIndexByNativeId(event, inputDevice);
    assertEquals(Linux.ERROR, index);
  }

  @Test
  void testIsBitSetForFfRumble() {
    byte[] ffBits = new byte[16];
    ffBits[10] = (byte) 0x01;
    assertTrue(LinuxEventDevice.isBitSet(ffBits, Linux.FF_RUMBLE));
    assertFalse(LinuxEventDevice.isBitSet(ffBits, Linux.FF_SINE));
    assertFalse(LinuxEventDevice.isBitSet(ffBits, Linux.FF_GAIN));
  }

  @Test
  void testIsBitSetForFfSine() {
    byte[] ffBits = new byte[16];
    ffBits[11] = (byte) 0x04;
    assertTrue(LinuxEventDevice.isBitSet(ffBits, Linux.FF_SINE));
    assertFalse(LinuxEventDevice.isBitSet(ffBits, Linux.FF_RUMBLE));
    assertFalse(LinuxEventDevice.isBitSet(ffBits, Linux.FF_GAIN));
  }

  @Test
  void testIsBitSetForFfGain() {
    byte[] ffBits = new byte[16];
    ffBits[12] = (byte) 0x01;
    assertTrue(LinuxEventDevice.isBitSet(ffBits, Linux.FF_GAIN));
    assertFalse(LinuxEventDevice.isBitSet(ffBits, Linux.FF_RUMBLE));
    assertFalse(LinuxEventDevice.isBitSet(ffBits, Linux.FF_SINE));
  }

  @Test
  void testIsBitSetForFfRumbleAndGain() {
    byte[] ffBits = new byte[16];
    ffBits[10] = (byte) 0x01;
    ffBits[12] = (byte) 0x01;
    assertTrue(LinuxEventDevice.isBitSet(ffBits, Linux.FF_RUMBLE));
    assertFalse(LinuxEventDevice.isBitSet(ffBits, Linux.FF_SINE));
    assertTrue(LinuxEventDevice.isBitSet(ffBits, Linux.FF_GAIN));
  }

  @Test
  void testGetMaxBitsForEvFfReturnsFfCnt() {
    assertEquals(Linux.FF_CNT, LinuxEventDevice.getMaxBits(LinuxEventDevice.EV_FF));
  }

  @Test
  void isGamepadOrJoystick_xboxGamepad_returnsTrue() {
    byte[] keyBits = createBitmask(LinuxInputDefinitions.BTN_A, LinuxInputDefinitions.BTN_B);
    byte[] absBits = createBitmask(LinuxInputDefinitions.ABS_X, LinuxInputDefinitions.ABS_Y);
    assertTrue(LinuxEventDevicePlugin.isGamepadOrJoystick(keyBits, absBits));
  }

  @Test
  void isGamepadOrJoystick_flightStick_returnsTrue() {
    byte[] keyBits = createBitmask(LinuxInputDefinitions.BTN_TRIGGER, LinuxInputDefinitions.BTN_THUMB);
    byte[] absBits = createBitmask(LinuxInputDefinitions.ABS_X, LinuxInputDefinitions.ABS_Y);
    assertTrue(LinuxEventDevicePlugin.isGamepadOrJoystick(keyBits, absBits));
  }

  @Test
  void isGamepadOrJoystick_arcadeStickTriggerHappy_returnsTrue() {
    byte[] keyBits = createBitmask(LinuxInputDefinitions.BTN_TRIGGER_HAPPY1);
    assertTrue(LinuxEventDevicePlugin.isGamepadOrJoystick(keyBits, null));
  }

  @Test
  void isGamepadOrJoystick_rudderPedals_returnsTrue() {
    byte[] absBits = createBitmask(LinuxInputDefinitions.ABS_RUDDER, LinuxInputDefinitions.ABS_BRAKE);
    assertTrue(LinuxEventDevicePlugin.isGamepadOrJoystick(null, absBits));
  }

  @Test
  void isGamepadOrJoystick_hatSwitch_returnsTrue() {
    byte[] absBits = createBitmask(LinuxInputDefinitions.ABS_HAT0X, LinuxInputDefinitions.ABS_HAT0Y);
    assertTrue(LinuxEventDevicePlugin.isGamepadOrJoystick(null, absBits));
  }

  @Test
  void isGamepadOrJoystick_touchpad_returnsFalse() {
    byte[] keyBits = createBitmask(LinuxInputDefinitions.BTN_TOUCH, LinuxInputDefinitions.BTN_TOOL_FINGER, LinuxInputDefinitions.BTN_LEFT);
    byte[] absBits = createBitmask(LinuxInputDefinitions.ABS_X, LinuxInputDefinitions.ABS_Y);
    assertFalse(LinuxEventDevicePlugin.isGamepadOrJoystick(keyBits, absBits));
  }

  @Test
  void isGamepadOrJoystick_mouse_returnsFalse() {
    byte[] keyBits = createBitmask(LinuxInputDefinitions.BTN_LEFT, LinuxInputDefinitions.BTN_RIGHT, LinuxInputDefinitions.BTN_MIDDLE);
    assertFalse(LinuxEventDevicePlugin.isGamepadOrJoystick(keyBits, null));
  }

  @Test
  void isGamepadOrJoystick_keyboard_returnsFalse() {
    byte[] keyBits = createBitmask(LinuxComponentType.KEY_A.getCode(), LinuxComponentType.KEY_ENTER.getCode());
    assertFalse(LinuxEventDevicePlugin.isGamepadOrJoystick(keyBits, null));
  }

  @Test
  void isGamepadOrJoystick_nullOrEmpty_returnsFalse() {
    assertFalse(LinuxEventDevicePlugin.isGamepadOrJoystick(null, null));
    assertFalse(LinuxEventDevicePlugin.isGamepadOrJoystick(new byte[0], new byte[0]));
  }

  @Test
  void isGamepadOrJoystick_gamepadWithTouchpad_returnsTrue() {
    // DualShock / DualSense having both gamepad buttons and touchpad tool bits on the same device
    byte[] keyBits = createBitmask(LinuxInputDefinitions.BTN_A, LinuxInputDefinitions.BTN_TOUCH);
    byte[] absBits = createBitmask(LinuxInputDefinitions.ABS_X, LinuxInputDefinitions.ABS_Y);
    assertTrue(LinuxEventDevicePlugin.isGamepadOrJoystick(keyBits, absBits));
  }

  private static byte[] createBitmask(int... bits) {
    int maxBit = 0;
    for (int b : bits) {
      if (b > maxBit) {
        maxBit = b;
      }
    }
    byte[] array = new byte[(maxBit / 8) + 1];
    for (int b : bits) {
      array[b / 8] |= (byte) (1 << (b % 8));
    }
    return array;
  }
}
