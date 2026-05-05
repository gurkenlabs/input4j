package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.components.Axis;
import de.gurkenlabs.input4j.components.Button;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LinuxInputMappingsTests {

  @Test
  void testRegisterAndLookupButtonMapping() {
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0, Button.BUTTON_0);

    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0);

    assertTrue(result.isPresent());
    assertEquals(Button.BUTTON_0, result.get());
  }

  @Test
  void testRegisterAndLookupAxisMapping() {
    LinuxInputMappings.registerAxisMapping(0x9999, 0x8888, "TestController", LinuxEventCode.ABS_X, Axis.AXIS_X);

    Optional<InputComponent.ID> result = LinuxInputMappings.getAxisMapping(0x9999, 0x8888, "TestController", LinuxEventCode.ABS_X);

    assertTrue(result.isPresent());
    assertEquals(Axis.AXIS_X, result.get());
  }

  @Test
  void testPrefixMatching() {
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "Test%", LinuxEventCode.BTN_0, Button.BUTTON_0);

    Optional<InputComponent.ID> result1 = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0);
    Optional<InputComponent.ID> result2 = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestGamepad", LinuxEventCode.BTN_0);
    Optional<InputComponent.ID> result3 = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "Xbox Controller", LinuxEventCode.BTN_0);

    assertTrue(result1.isPresent());
    assertTrue(result2.isPresent());
    assertTrue(result3.isEmpty());
  }

  @Test
  void testWildcardAnyVendor() {
    LinuxInputMappings.registerButtonMapping(0x9999, -1, "TestController%", LinuxEventCode.BTN_0, Button.BUTTON_0);

    Optional<InputComponent.ID> result1 = LinuxInputMappings.getButtonMapping(0x9999, 0x1234, "TestController X", LinuxEventCode.BTN_0);
    Optional<InputComponent.ID> result2 = LinuxInputMappings.getButtonMapping(0x9999, 0x5678, "TestController Y", LinuxEventCode.BTN_0);
    Optional<InputComponent.ID> result3 = LinuxInputMappings.getButtonMapping(0x999A, 0x1234, "TestController Z", LinuxEventCode.BTN_0);

    assertTrue(result1.isPresent());
    assertTrue(result2.isPresent());
    assertTrue(result3.isEmpty());
  }

  @Test
  void testGenericFallbackNoVidNoPid() {
    LinuxInputMappings.registerButtonMapping(-1, -1, "Generic Gamepad%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    LinuxInputMappings.registerButtonMapping(-1, -1, "Generic Gamepad%", LinuxEventCode.BTN_1, Button.BUTTON_1);

    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(-1, -1, "Generic Gamepad USB", LinuxEventCode.BTN_0);

    assertTrue(result.isPresent());
    assertEquals(Button.BUTTON_0, result.get());
  }

  @Test
  void testEventCodeMismatch() {
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0, Button.BUTTON_0);

    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_1);

    assertTrue(result.isEmpty());
  }

  @Test
  void testDeviceNameMismatch() {
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0, Button.BUTTON_0);

    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "Different Controller", LinuxEventCode.BTN_0);

    assertTrue(result.isEmpty());
  }

  @Test
  void testMultipleButtonMappings() {
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0, Button.BUTTON_0);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_1, Button.BUTTON_1);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_2, Button.BUTTON_2);

    assertTrue(LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0).isPresent());
    assertTrue(LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_1).isPresent());
    assertTrue(LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_2).isPresent());
    assertTrue(LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_3).isEmpty());
  }

  @Test
  void testSpecificMappingTakesPrecedence() {
    LinuxInputMappings.registerButtonMapping(-1, -1, "Generic%", LinuxEventCode.BTN_0, Button.BUTTON_3);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "Test%", LinuxEventCode.BTN_0, Button.BUTTON_0);

    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0);

    assertTrue(result.isPresent());
    assertEquals(Button.BUTTON_0, result.get());
  }

  @Test
  void testBuiltInMappingsLoaded() {
    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(0x0079, 0x1234, "DragonRise Generic", LinuxEventCode.BTN_0);

    assertTrue(result.isPresent());
    assertEquals(Button.BUTTON_0, result.get());
  }

  @Test
  void testLookupWithNullDeviceName() {
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0, Button.BUTTON_0);

    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, null, LinuxEventCode.BTN_0);

    assertTrue(result.isEmpty());
  }

  @Test
  void testLookupWithDifferentProductId() {
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestController", LinuxEventCode.BTN_0, Button.BUTTON_0);

    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(0x9999, 0x9999, "TestController", LinuxEventCode.BTN_0);

    assertTrue(result.isEmpty());
  }

  @Test
  void testDifferentVendorWithSameProductId() {
    LinuxInputMappings.registerButtonMapping(0x1111, 0x8888, "TestController", LinuxEventCode.BTN_0, Button.BUTTON_0);
    LinuxInputMappings.registerButtonMapping(0x2222, 0x8888, "TestController", LinuxEventCode.BTN_0, Button.BUTTON_1);

    Optional<InputComponent.ID> result1 = LinuxInputMappings.getButtonMapping(0x1111, 0x8888, "TestController", LinuxEventCode.BTN_0);
    Optional<InputComponent.ID> result2 = LinuxInputMappings.getButtonMapping(0x2222, 0x8888, "TestController", LinuxEventCode.BTN_0);

    assertTrue(result1.isPresent());
    assertEquals(Button.BUTTON_0, result1.get());
    assertTrue(result2.isPresent());
    assertEquals(Button.BUTTON_1, result2.get());
  }

  @Test
  void testAxisMappingLookup() {
    LinuxInputMappings.registerAxisMapping(0x9999, 0x8888, "TestController", LinuxEventCode.ABS_Z, Axis.AXIS_Z);
    LinuxInputMappings.registerAxisMapping(0x9999, 0x8888, "TestController", LinuxEventCode.ABS_RZ, Axis.AXIS_RZ);

    Optional<InputComponent.ID> resultZ = LinuxInputMappings.getAxisMapping(0x9999, 0x8888, "TestController", LinuxEventCode.ABS_Z);
    Optional<InputComponent.ID> resultRZ = LinuxInputMappings.getAxisMapping(0x9999, 0x8888, "TestController", LinuxEventCode.ABS_RZ);
    Optional<InputComponent.ID> resultX = LinuxInputMappings.getAxisMapping(0x9999, 0x8888, "TestController", LinuxEventCode.ABS_X);

    assertTrue(resultZ.isPresent());
    assertEquals(Axis.AXIS_Z, resultZ.get());
    assertTrue(resultRZ.isPresent());
    assertEquals(Axis.AXIS_RZ, resultRZ.get());
    assertTrue(resultX.isEmpty());
  }

  @Test
  void testBuiltInAxisMappings() {
    Optional<InputComponent.ID> result = LinuxInputMappings.getAxisMapping(0x05AC, 0x03DD, "GameSir G3", LinuxEventCode.ABS_Z);

    assertTrue(result.isPresent());
    assertEquals(Axis.AXIS_Z, result.get());
  }

  @Test
  void testGetButtonMappingsForDevice() {
    LinuxInputMappings.clearCustomMappings();
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestDevice%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestDevice%", LinuxEventCode.BTN_1, Button.BUTTON_1);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "TestDevice%", LinuxEventCode.BTN_2, Button.BUTTON_2);
    LinuxInputMappings.registerAxisMapping(0x9999, 0x8888, "TestDevice%", LinuxEventCode.ABS_X, Axis.AXIS_X);

    List<LinuxInputMappings.MappingInfo> buttonMappings = LinuxInputMappings.getButtonMappingsForDevice(0x9999, 0x8888, "TestDevice One");
    List<LinuxInputMappings.MappingInfo> axisMappings = LinuxInputMappings.getAxisMappingsForDevice(0x9999, 0x8888, "TestDevice One");

    assertEquals(3, buttonMappings.size());
    assertEquals(1, axisMappings.size());
  }

  @Test
  void testGetMappingsForDeviceFiltersByVidPid() {
    LinuxInputMappings.clearCustomMappings();
    LinuxInputMappings.registerButtonMapping(0x1111, 0x2222, "MyDevice%", LinuxEventCode.BTN_0, Button.BUTTON_0);
    LinuxInputMappings.registerButtonMapping(0x3333, 0x4444, "MyDevice%", LinuxEventCode.BTN_0, Button.BUTTON_1);

    List<LinuxInputMappings.MappingInfo> mappings1 = LinuxInputMappings.getButtonMappingsForDevice(0x1111, 0x2222, "MyDevice X");
    List<LinuxInputMappings.MappingInfo> mappings2 = LinuxInputMappings.getButtonMappingsForDevice(0x3333, 0x4444, "MyDevice Y");

    assertEquals(1, mappings1.size());
    assertEquals(Button.BUTTON_0, mappings1.get(0).inputId());
    assertEquals(1, mappings2.size());
    assertEquals(Button.BUTTON_1, mappings2.get(0).inputId());
  }

  @Test
  void testControllerTypeFallbackMappings() {
    Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(-1, -1, "Generic USB Gamepad", LinuxEventCode.BTN_0);

    assertTrue(result.isPresent());
    assertEquals(Button.BUTTON_0, result.get());
  }

  @Test
  void testMultipleButtons() {
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "FullController", LinuxEventCode.BTN_0, Button.BUTTON_0);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "FullController", LinuxEventCode.BTN_1, Button.BUTTON_1);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "FullController", LinuxEventCode.BTN_2, Button.BUTTON_2);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "FullController", LinuxEventCode.BTN_3, Button.BUTTON_3);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "FullController", LinuxEventCode.BTN_4, Button.BUTTON_4);
    LinuxInputMappings.registerButtonMapping(0x9999, 0x8888, "FullController", LinuxEventCode.BTN_5, Button.BUTTON_5);

    for (int i = 0; i <= 5; i++) {
      Optional<InputComponent.ID> result = LinuxInputMappings.getButtonMapping(0x9999, 0x8888, "FullController", LinuxEventCode.BTN_0 + i);
      assertTrue(result.isPresent(), "Button " + i + " should be present");
    }
  }
}