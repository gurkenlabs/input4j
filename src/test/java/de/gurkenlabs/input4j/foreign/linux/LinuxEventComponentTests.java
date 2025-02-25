package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.components.XInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LinuxEventComponentTests {

  @Test
  public void testConvertValue() {
    LinuxEventComponent component = new LinuxEventComponent(LinuxComponentType.ABS_X, true, false, 3, 4, -100, 100, 0, 0);

    // Test value within range
    assertEquals(0.0f, component.convertValue(0.0f));
    assertEquals(1.0f, component.convertValue(100.0f));
    assertEquals(-1.0f, component.convertValue(-100.0f));

    // Test value exceeding max
    assertEquals(1.0f, component.convertValue(150.0f));

    // Test value below min
    assertEquals(-1.0f, component.convertValue(-150.0f));

    // Test when min equals max
    LinuxEventComponent componentWithEqualMinMax = new LinuxEventComponent(LinuxComponentType.ABS_X, true, false, 3, 4, 100, 100, 0, 0);
    assertEquals(0.0f, componentWithEqualMinMax.convertValue(100.0f));
  }

  @Test
  public void testIsRelative() {
    LinuxEventComponent relativeComponent = new LinuxEventComponent(LinuxComponentType.ABS_X, true, true, 3, 4);
    assertTrue(relativeComponent.isRelative());

    LinuxEventComponent absoluteComponent = new LinuxEventComponent(LinuxComponentType.ABS_X, true, false, 3, 4);
    assertFalse(absoluteComponent.isRelative());
  }

  @Test
  public void testIsAnalog() {
    LinuxEventComponent analogComponent = new LinuxEventComponent(LinuxComponentType.ABS_X, true, false, 3, 4);
    assertTrue(analogComponent.isAnalog());

    LinuxEventComponent digitalComponent = new LinuxEventComponent(LinuxComponentType.BTN_SOUTH, false, false, 1, 2);
    assertFalse(digitalComponent.isAnalog());
  }

  @Test
  public void testEquals() {
    LinuxEventComponent component1 = new LinuxEventComponent(LinuxComponentType.ABS_X, true, false, 3, 4);
    LinuxEventComponent component2 = new LinuxEventComponent(LinuxComponentType.ABS_X, true, false, 3, 4);
    LinuxEventComponent component3 = new LinuxEventComponent(LinuxComponentType.ABS_Y, true, false, 3, 5);

    assertEquals(component1, component2);
    assertNotEquals(component1, component3);
  }

  @Test
  public void testGetIdentifier() {
    LinuxEventComponent component = new LinuxEventComponent(LinuxComponentType.BTN_SOUTH, false, false, 1, 2);
    InputComponent.ID id = component.getIdentifier();
    assertEquals(XInput.A, id);
    assertEquals(2, id.nativeId);
  }
}