package de.gurkenlabs.input4j.foreign.windows.xinput;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XInputDataStructTests {
  @Test
  void testXInputVibration() {
    var vibration = new XINPUT_VIBRATION();
    vibration.wLeftMotorSpeed = 100;
    vibration.wRightMotorSpeed = 200;

    try (var memorySession = Arena.ofConfined()) {
      var segment = memorySession.allocate(XINPUT_VIBRATION.$LAYOUT);
      vibration.write(segment);

      var testVibration = XINPUT_VIBRATION.read(segment);
      assertEquals(vibration.wLeftMotorSpeed, testVibration.wLeftMotorSpeed);
      assertEquals(vibration.wRightMotorSpeed, testVibration.wRightMotorSpeed);
    }
  }

  @Test
  void testXInputGamepad() {
    var gamepad = new XINPUT_GAMEPAD();
    gamepad.wButtons = 1;
    gamepad.bLeftTrigger = 2;
    gamepad.bRightTrigger = 3;
    gamepad.sThumbLX = 4;
    gamepad.sThumbLY = 5;
    gamepad.sThumbRX = 6;
    gamepad.sThumbRY = 7;

    try (var memorySession = Arena.ofConfined()) {
      var segment = memorySession.allocate(XINPUT_GAMEPAD.$LAYOUT);
      gamepad.write(segment);

      var testGamepad = XINPUT_GAMEPAD.read(segment);
      assertEquals(gamepad.wButtons, testGamepad.wButtons);
      assertEquals(gamepad.bLeftTrigger, testGamepad.bLeftTrigger);
      assertEquals(gamepad.bRightTrigger, testGamepad.bRightTrigger);
      assertEquals(gamepad.sThumbLX, testGamepad.sThumbLX);
      assertEquals(gamepad.sThumbLY, testGamepad.sThumbLY);
      assertEquals(gamepad.sThumbRX, testGamepad.sThumbRX);
      assertEquals(gamepad.sThumbRY, testGamepad.sThumbRY);
    }
  }

  @Test
  void testXInputState() {
    var state = new XINPUT_STATE();
    state.dwPacketNumber = 1234;
    state.Gamepad = new XINPUT_GAMEPAD();
    state.Gamepad.wButtons = 1;
    state.Gamepad.bLeftTrigger = 2;
    state.Gamepad.bRightTrigger = 3;
    state.Gamepad.sThumbLX = 4;
    state.Gamepad.sThumbLY = 5;
    state.Gamepad.sThumbRX = 6;
    state.Gamepad.sThumbRY = 7;

    try (var memorySession = Arena.ofConfined()) {
      var segment = memorySession.allocate(XINPUT_STATE.$LAYOUT);
      state.write(segment);

      var testState = XINPUT_STATE.read(segment);
      assertEquals(state.dwPacketNumber, testState.dwPacketNumber);
      assertEquals(state.Gamepad.wButtons, testState.Gamepad.wButtons);
      assertEquals(state.Gamepad.bLeftTrigger, testState.Gamepad.bLeftTrigger);
      assertEquals(state.Gamepad.bRightTrigger, testState.Gamepad.bRightTrigger);
      assertEquals(state.Gamepad.sThumbLX, testState.Gamepad.sThumbLX);
      assertEquals(state.Gamepad.sThumbLY, testState.Gamepad.sThumbLY);
      assertEquals(state.Gamepad.sThumbRX, testState.Gamepad.sThumbRX);
      assertEquals(state.Gamepad.sThumbRY, testState.Gamepad.sThumbRY);
    }
  }

  @Test
  void testXInputCapabilities() {
    var capabilities = new XINPUT_CAPABILITIES();
    capabilities.Type = 1;
    capabilities.SubType = 2;
    capabilities.Flags = 3;
    capabilities.Gamepad = new XINPUT_GAMEPAD();
    capabilities.Gamepad.wButtons = 1;
    capabilities.Gamepad.bLeftTrigger = 2;
    capabilities.Gamepad.bRightTrigger = 3;
    capabilities.Gamepad.sThumbLX = 4;
    capabilities.Gamepad.sThumbLY = 5;
    capabilities.Gamepad.sThumbRX = 6;
    capabilities.Gamepad.sThumbRY = 7;
    capabilities.Vibration = new XINPUT_VIBRATION();
    capabilities.Vibration.wLeftMotorSpeed = 100;
    capabilities.Vibration.wRightMotorSpeed = 200;

    try (var memorySession = Arena.ofConfined()) {
      var segment = memorySession.allocate(XINPUT_CAPABILITIES.$LAYOUT);
      capabilities.write(segment);

      var testCapabilities = XINPUT_CAPABILITIES.read(segment);
      assertEquals(capabilities.Type, testCapabilities.Type);
      assertEquals(capabilities.SubType, testCapabilities.SubType);
      assertEquals(capabilities.Flags, testCapabilities.Flags);
      assertEquals(capabilities.Gamepad.wButtons, testCapabilities.Gamepad.wButtons);
      assertEquals(capabilities.Gamepad.bLeftTrigger, testCapabilities.Gamepad.bLeftTrigger);
      assertEquals(capabilities.Gamepad.bRightTrigger, testCapabilities.Gamepad.bRightTrigger);
      assertEquals(capabilities.Gamepad.sThumbLX, testCapabilities.Gamepad.sThumbLX);
      assertEquals(capabilities.Gamepad.sThumbLY, testCapabilities.Gamepad.sThumbLY);
      assertEquals(capabilities.Gamepad.sThumbRX, testCapabilities.Gamepad.sThumbRX);
      assertEquals(capabilities.Gamepad.sThumbRY, testCapabilities.Gamepad.sThumbRY);
      assertEquals(capabilities.Vibration.wLeftMotorSpeed, testCapabilities.Vibration.wLeftMotorSpeed);
      assertEquals(capabilities.Vibration.wRightMotorSpeed, testCapabilities.Vibration.wRightMotorSpeed);
    }
  }
}
