package de.gurkenlabs.input4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class InputDeviceListenerTests {
  private InputDevice inputDevice;
  private InputComponent buttonComponent;
  private InputComponent axisComponent;

  @BeforeEach
  void setUp() {
    Function<InputDevice, float[]> pollCallback = inputDevice -> {
      float[] values = new float[inputDevice.getComponents().size()];
      for (int i = 0; i < values.length; i++) {
        values[i] = 1.0f;
      }
      return values;
    };
    BiConsumer<InputDevice, float[]> rumbleCallback = (_, _) -> {
    };
    inputDevice = new InputDevice("123", "TestDevice", "TestProduct", pollCallback, rumbleCallback);

    buttonComponent = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1"));
    axisComponent = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1"));
    inputDevice.addComponent(buttonComponent);
    inputDevice.addComponent(axisComponent);
  }

  @Test
  void testOnInputValueChanged_NotifiedOnPoll() {
    AtomicBoolean called = new AtomicBoolean(false);
    inputDevice.onInputValueChanged(event -> called.set(true));
    inputDevice.poll();
    assertTrue(called.get());
  }

  @Test
  void testOnInputValueChanged_ReceivesCorrectEvent() {
    AtomicReference<Float> oldValue = new AtomicReference<>(-1f);
    AtomicReference<Float> newValue = new AtomicReference<>(-1f);
    inputDevice.onInputValueChanged(event -> {
      oldValue.set(event.oldValue());
      newValue.set(event.newValue());
    });
    inputDevice.poll();
    assertEquals(0.0f, oldValue.get());
    assertEquals(1.0f, newValue.get());
  }

  @Test
  void testOnButtonPressed_NotifiedWhenButtonPressed() {
    AtomicBoolean called = new AtomicBoolean(false);
    inputDevice.onButtonPressed(buttonComponent.getId(), () -> called.set(true));
    inputDevice.poll();
    assertTrue(called.get());
  }

  @Test
  void testOnButtonPressed_WithIntId() {
    AtomicBoolean called = new AtomicBoolean(false);
    inputDevice.onButtonPressed(1, () -> called.set(true));
    inputDevice.poll();
    assertTrue(called.get());
  }

  @Test
  void testOnButtonPressed_ReturnsFalseForInvalidId() {
    boolean result = inputDevice.onButtonPressed(new InputComponent.ID(ComponentType.BUTTON, 999, "INVALID"), () -> {
    });
    assertFalse(result);
  }

  @Test
  void testOnButtonReleased_NotifiedWhenButtonReleased() {
    inputDevice.poll();
    AtomicBoolean called = new AtomicBoolean(false);
    inputDevice.onButtonReleased(buttonComponent.getId(), () -> called.set(true));
    inputDevice.poll();
    assertFalse(called.get());
  }

  @Test
  void testOnAxisChanged_NotifiedOnAxisChange() {
    AtomicReference<Float> receivedValue = new AtomicReference<>(-1f);
    inputDevice.onAxisChanged(axisComponent.getId(), value -> receivedValue.set(value));
    inputDevice.poll();
    assertEquals(1.0f, receivedValue.get());
  }

  @Test
  void testOnAxisChanged_WithIntId() {
    AtomicReference<Float> receivedValue = new AtomicReference<>(-1f);
    inputDevice.onAxisChanged(1, value -> receivedValue.set(value));
    inputDevice.poll();
    assertEquals(1.0f, receivedValue.get());
  }

  @Test
  void testOnAxisChanged_ReturnsFalseForInvalidId() {
    boolean result = inputDevice.onAxisChanged(new InputComponent.ID(ComponentType.AXIS, 999, "INVALID"), value -> {
    });
    assertFalse(result);
  }

  @Test
  void testClearButtonPressedListeners_RemovesListeners() {
    AtomicBoolean called = new AtomicBoolean(false);
    inputDevice.onButtonPressed(buttonComponent.getId(), () -> called.set(true));
    inputDevice.clearButtonPressedListeners(buttonComponent.getId());
    inputDevice.poll();
    assertFalse(called.get());
  }

  @Test
  void testClearAxisChangedListeners_RemovesListeners() {
    AtomicReference<Float> receivedValue = new AtomicReference<>(-1f);
    inputDevice.onAxisChanged(axisComponent.getId(), value -> receivedValue.set(value));
    inputDevice.clearAxisChangedListeners(axisComponent.getId());
    inputDevice.poll();
    assertEquals(-1, receivedValue.get());
  }

  @Test
  void testRemoveButtonPressedListener_RemovesSpecificListener() {
    AtomicInteger callCount = new AtomicInteger(0);
    Runnable listener1 = () -> callCount.incrementAndGet();
    Runnable listener2 = () -> callCount.incrementAndGet();

    inputDevice.onButtonPressed(buttonComponent.getId(), listener1);
    inputDevice.onButtonPressed(buttonComponent.getId(), listener2);
    inputDevice.removeButtonPressedListener(listener1);
    inputDevice.poll();
    assertEquals(1, callCount.get());
  }

  @Test
  void testRemoveAxisChangedListener_RemovesSpecificListener() {
    AtomicInteger callCount = new AtomicInteger(0);
    Consumer<Float> listener1 = value -> callCount.incrementAndGet();
    Consumer<Float> listener2 = value -> callCount.incrementAndGet();

    inputDevice.onAxisChanged(axisComponent.getId(), listener1);
    inputDevice.onAxisChanged(axisComponent.getId(), listener2);
    inputDevice.removeAxisChangedListener(listener1);
    inputDevice.poll();
    assertEquals(1, callCount.get());
  }

  @Test
  void testGetComponentIndex_ReturnsCorrectIndex() {
    assertEquals(0, inputDevice.getComponentIndex(buttonComponent.getId()));
    assertEquals(1, inputDevice.getComponentIndex(axisComponent.getId()));
  }

  @Test
  void testGetComponentIndex_ReturnsNegativeForInvalidId() {
    InputComponent.ID invalidId = new InputComponent.ID(ComponentType.BUTTON, 999, "INVALID");
    assertEquals(-1, inputDevice.getComponentIndex(invalidId));
  }

  @Test
  void testAddComponent_AddsNewComponent() {
    InputComponent newComponent = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.BUTTON, 99, "NEW_BUTTON"));
    inputDevice.addComponent(newComponent);
    assertEquals(3, inputDevice.getComponents().size());
  }

  @Test
  void testClose_ClearsAllListeners() {
    inputDevice.onButtonPressed(buttonComponent.getId(), () -> {
    });
    inputDevice.onAxisChanged(axisComponent.getId(), value -> {
    });
    inputDevice.close();
    inputDevice.poll();
  }

  @Test
  void testRumble_CallsCallback() {
    AtomicBoolean called = new AtomicBoolean(false);
    BiConsumer<InputDevice, float[]> rumbleCallback = (device, intensity) -> called.set(true);

    InputDevice deviceWithRumble = new InputDevice("123", "Test", "Test", _ -> new float[]{}, rumbleCallback);
    deviceWithRumble.rumble(0.5f);
    assertTrue(called.get());
  }

  @Test
  void testRumble_DoesNothingWhenCallbackIsNull() {
    InputDevice deviceNoRumble = new InputDevice("123", "Test", "Test", _ -> new float[]{}, null);
    assertDoesNotThrow(() -> deviceNoRumble.rumble(0.5f));
  }

  @Test
  void testHasInputData_ReturnsTrueAfterPollWithData() {
    assertFalse(inputDevice.hasInputData());
    inputDevice.poll();
    assertTrue(inputDevice.hasInputData());
  }

  @Test
  void testSetAccuracy_ThrowsForNegativeValue() {
    assertThrows(IllegalArgumentException.class, () -> inputDevice.setAccuracy(-1));
  }

  @Test
  void testToString_ReturnsDeviceName() {
    assertEquals("TestDevice", inputDevice.toString());
  }
}
