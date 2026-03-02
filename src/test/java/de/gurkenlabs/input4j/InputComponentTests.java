package de.gurkenlabs.input4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class InputComponentTests {
  private InputDevice inputDevice;

  @BeforeEach
  void setUp() {
    List<InputComponent> components = new CopyOnWriteArrayList<>();
    Function<InputDevice, float[]> pollCallback = _ -> new float[]{};
    BiConsumer<InputDevice, float[]> rumbleCallback = (_, _) -> {
    };
    inputDevice = new InputDevice("123", "TestDevice", "TestProduct", pollCallback, rumbleCallback);
    inputDevice.setComponents(components);
  }

  @Test
  void testIsButton_ReturnsTrueForButtonComponent() {
    InputComponent.ID buttonId = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent component = new InputComponent(inputDevice, buttonId);
    assertTrue(component.isButton());
  }

  @Test
  void testIsButton_ReturnsFalseForAxisComponent() {
    InputComponent.ID axisId = new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1");
    InputComponent component = new InputComponent(inputDevice, axisId);
    assertFalse(component.isButton());
  }

  @Test
  void testIsAxis_ReturnsTrueForAxisComponent() {
    InputComponent.ID axisId = new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1");
    InputComponent component = new InputComponent(inputDevice, axisId);
    assertTrue(component.isAxis());
  }

  @Test
  void testIsAxis_ReturnsFalseForButtonComponent() {
    InputComponent.ID buttonId = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent component = new InputComponent(inputDevice, buttonId);
    assertFalse(component.isAxis());
  }

  @Test
  void testIsRelative_ReturnsFalseByDefault() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1");
    InputComponent component = new InputComponent(inputDevice, id);
    assertFalse(component.isRelative());
  }

  @Test
  void testIsRelative_ReturnsTrueWhenSet() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1");
    InputComponent component = new InputComponent(inputDevice, id, true);
    assertTrue(component.isRelative());
  }

  @Test
  void testGetOriginalName_ReturnsName() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent component = new InputComponent(inputDevice, id, "OriginalButton");
    assertEquals("OriginalButton", component.getOriginalName());
  }

  @Test
  void testGetOriginalName_ReturnsNullWhenNotSet() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent component = new InputComponent(inputDevice, id);
    assertNull(component.getOriginalName());
  }

  @Test
  void testGetDevice_ReturnsInputDevice() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent component = new InputComponent(inputDevice, id);
    assertEquals(inputDevice, component.getDevice());
  }

  @Test
  void testGetType_ReturnsComponentType() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1");
    InputComponent component = new InputComponent(inputDevice, id);
    assertEquals(ComponentType.AXIS, component.getType());
  }

  @Test
  void testSetData_StoresValue() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1");
    InputComponent component = new InputComponent(inputDevice, id);
    component.setData(0.5f);
    assertEquals(0.5f, component.getData());
  }

  @Test
  void testToString_FormatsCorrectly() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent component = new InputComponent(inputDevice, id);
    component.setData(1.0f);
    assertEquals("BUTTON_1: 1.0", component.toString());
  }
}
