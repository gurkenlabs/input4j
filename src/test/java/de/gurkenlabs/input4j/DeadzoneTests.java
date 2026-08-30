package de.gurkenlabs.input4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeadzoneTests {
  private static final class TestPlugin extends AbstractInputDevicePlugin {
    @Override
    public void internalInitDevices(java.awt.Frame owner) {
      // The test only exposes inherited deadzone helpers; no input devices are initialized.
    }

    @Override
    protected java.util.Collection<InputDevice> refreshInputDevices() {
      return java.util.List.of();
    }

    static void circular(float[] values, float deadzone) {
      applyCircularDeadzone(values, 0, 1, deadzone);
    }

    static float scalar(float value, float deadzone) {
      return applyDeadzone(value, deadzone);
    }
  }

  @Test
  void circularDeadzone_preservesDiagonalOutsideRadius() {
    var values = new float[] {0.6f, 0.6f};

    TestPlugin.circular(values, 0.75f);

    assertTrue(values[0] > 0f);
    assertEquals(values[0], values[1], 0.000001f);
  }

  @Test
  void circularDeadzone_rescalesValuesAndKeepsMaximum() {
    var values = new float[] {0.8f, 0f};
    TestPlugin.circular(values, 0.2f);
    assertEquals(0.75f, values[0], 0.000001f);

    values = new float[] {1f, 0f};
    TestPlugin.circular(values, 0.2f);
    assertEquals(1f, values[0], 0.000001f);
  }

  @Test
  void circularDeadzone_zerosValuesInsideCircle() {
    var values = new float[] {0.5f, 0.5f};

    TestPlugin.circular(values, 0.75f);

    assertEquals(0f, values[0]);
    assertEquals(0f, values[1]);
  }

  @Test
  void scalarDeadzone_preservesSmallValuesOutsideThreshold() {
    assertEquals(0.0625f, TestPlugin.scalar(0.25f, 0.2f), 0.000001f);
    assertEquals(0f, TestPlugin.scalar(0.2f, 0.2f));
  }
}
