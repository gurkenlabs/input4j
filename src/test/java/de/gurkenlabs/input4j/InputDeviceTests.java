package de.gurkenlabs.input4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class InputDeviceTests {
  private InputDevice inputDevice;

  @BeforeEach
  public void setUp() {
    List<InputComponent> components = new CopyOnWriteArrayList<>();
    Function<InputDevice, float[]> pollCallback = _ -> new float[]{0.12345f};
    BiConsumer<InputDevice, float[]> rumbleCallback = (_, _) -> {
    };

    inputDevice = new InputDevice("123", "TestInstance", "TestProduct", pollCallback, rumbleCallback);
    inputDevice.setComponents(components);
  }

  @Test
  public void testGetInstanceName() {
    assertEquals("TestInstance", inputDevice.getName());
  }

  @Test
  public void testGetProductName() {
    assertEquals("TestProduct", inputDevice.getProductName());
  }

  @Test
  public void testGetComponents() {
    assertTrue(inputDevice.getComponents().isEmpty());
  }

  @Test
  public void testAddComponent() {
    InputComponent component = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1"));
    inputDevice.addComponent(component);
    assertEquals(1, inputDevice.getComponents().size());
    assertEquals(component, inputDevice.getComponents().getFirst());
  }

  @Test
  public void testGetComponentByName() {
    InputComponent component = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1"));
    inputDevice.addComponent(component);
    Optional<InputComponent> retrievedComponent = inputDevice.getComponent("BUTTON_1");
    assertTrue(retrievedComponent.isPresent());
    assertEquals(component, retrievedComponent.get());
  }

  @Test
  public void testGetComponentById() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent component = new InputComponent(inputDevice, id);
    inputDevice.addComponent(component);
    Optional<InputComponent> retrievedComponent = inputDevice.getComponent(id);
    assertTrue(retrievedComponent.isPresent());
    assertEquals(component, retrievedComponent.get());
  }

  @Test
  public void testPoll() {
    InputComponent component = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1"));
    inputDevice.addComponent(component);
    inputDevice.poll();
    assertTrue(inputDevice.hasInputData());
  }

  @Test
  public void testRumble() {
    float[] intensity = {0.5f, 0.5f};
    inputDevice.rumble(intensity);
    // No assertion needed as rumbleCallback is a no-op in this test
  }

  @Test
  public void testClose() {
    InputComponent component = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1"));
    inputDevice.addComponent(component);
    assertEquals(1, inputDevice.getComponents().size());

    inputDevice.close();
    assertTrue(inputDevice.getComponents().isEmpty());
    assertTrue(inputDevice.getComponent("BUTTON_1").isEmpty());
  }

  @Test
  public void testSetAccuracy() {
    inputDevice.setAccuracy(2);
    InputComponent component = new InputComponent(inputDevice, new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1"));
    inputDevice.addComponent(component);

    inputDevice.poll();

    // Verify that the component data is rounded to 2 decimal places
    assertEquals(0.12f, component.getData());
  }

  @Test
  public void testHasInputData() {
    assertFalse(inputDevice.hasInputData());
  }

  @Test
  public void testGetVendorId_DefaultMinusOne() {
    assertEquals(-1, inputDevice.getVendorId());
  }

  @Test
  public void testGetProductId_DefaultMinusOne() {
    assertEquals(-1, inputDevice.getProductId());
  }

  @Test
  public void testHasVendorInfo_FalseByDefault() {
    assertFalse(inputDevice.hasVendorInfo());
  }

  @Test
  public void testGetControllerType_NoVendorInfo_ReturnsGeneric() {
    assertEquals(ControllerType.GENERIC, inputDevice.getControllerType());
  }

  @Test
  public void testGetDisplayName_FallsBackToProduct() {
    assertEquals("TestProduct", inputDevice.getDisplayName());
  }

  @Test
  public void testGetDisplayName_FallsBackToName() {
    InputDevice deviceNoProduct = new InputDevice("123", "TestInstance", null, _ -> new float[]{}, (_, _) -> {});
    assertEquals("TestInstance", deviceNoProduct.getDisplayName());
  }

  @Test
  public void testGetDisplayName_UsesDisplayName() {
    InputDevice device = new InputDevice("123", "TestInstance", "TestProduct", 
        ControllerDatabase.VENDOR_MICROSOFT, 0x028E, "Custom Display", _ -> new float[]{}, (_, _) -> {});
    assertEquals("Custom Display", device.getDisplayName());
  }

  @Test
  public void testGetVendorId_FromConstructor() {
    InputDevice device = new InputDevice("123", "Test", "Test", 
        ControllerDatabase.VENDOR_SONY, 0x0CE6, null, _ -> new float[]{}, (_, _) -> {});
    assertEquals(ControllerDatabase.VENDOR_SONY, device.getVendorId());
  }

  @Test
  public void testGetProductId_FromConstructor() {
    InputDevice device = new InputDevice("123", "Test", "Test", 
        ControllerDatabase.VENDOR_SONY, 0x0CE6, null, _ -> new float[]{}, (_, _) -> {});
    assertEquals(0x0CE6, device.getProductId());
  }

  @Test
  public void testHasVendorInfo_TrueWhenProvided() {
    InputDevice device = new InputDevice("123", "Test", "Test", 
        ControllerDatabase.VENDOR_MICROSOFT, 0x028E, null, _ -> new float[]{}, (_, _) -> {});
    assertTrue(device.hasVendorInfo());
  }

  @Test
  public void testGetControllerType_Xbox() {
    InputDevice device = new InputDevice("123", "Test", "Test", 
        ControllerDatabase.VENDOR_MICROSOFT, 0x028E, null, _ -> new float[]{}, (_, _) -> {});
    assertEquals(ControllerType.XBOX, device.getControllerType());
  }

  @Test
  public void testGetControllerType_PlayStation() {
    InputDevice device = new InputDevice("123", "Test", "Test", 
        ControllerDatabase.VENDOR_SONY, 0x0CE6, null, _ -> new float[]{}, (_, _) -> {});
    assertEquals(ControllerType.PLAYSTATION, device.getControllerType());
  }

  @Test
  public void testGetBatteryInfo_NullCallback() {
    assertTrue(inputDevice.getBatteryInfo().isEmpty());
  }

  @Test
  public void testGetBatteryInfo_WithCallback() {
    var expectedBattery = new BatteryInfo(BatteryType.UNKNOWN, BatteryLevel.FULL, false);
    InputDevice device = new InputDevice("123", "Test", "Test", -1, -1, null,
        _ -> new float[]{}, (_, _) -> {}, _ -> expectedBattery);
    var battery = device.getBatteryInfo();
    assertTrue(battery.isPresent());
    assertEquals(expectedBattery, battery.get());
  }

  @Test
  public void testPoll_NullPolledData() {
    InputDevice device = new InputDevice("123", "Test", "Test", _ -> null, (_, _) -> {});
    assertDoesNotThrow(device::poll);
  }

  @Test
  public void testPoll_ListenerExceptionDoesNotAbortPolling() {
    InputDevice device = new InputDevice("123", "Test", "Test", _ -> new float[]{1.0f, 1.0f}, (_, _) -> {});
    var btn1 = new InputComponent(device, new InputComponent.ID(ComponentType.BUTTON, 1, "BTN1"));
    var btn2 = new InputComponent(device, new InputComponent.ID(ComponentType.BUTTON, 2, "BTN2"));
    device.addComponent(btn1);
    device.addComponent(btn2);

    device.onButtonPressed(btn1.getId(), () -> {
      throw new RuntimeException("Simulated listener failure");
    });
    var secondListenerFired = new java.util.concurrent.atomic.AtomicBoolean(false);
    device.onButtonPressed(btn2.getId(), () -> secondListenerFired.set(true));

    assertDoesNotThrow(device::poll);
    assertTrue(secondListenerFired.get());
    assertEquals(1.0f, btn1.getData());
    assertEquals(1.0f, btn2.getData());
  }

  @Test
  public void testClose_StopsRumble() {
    var rumbleValues = new java.util.concurrent.atomic.AtomicReference<float[]>();
    InputDevice device = new InputDevice("123", "Test", "Test", _ -> new float[0], (_, val) -> rumbleValues.set(val));
    device.close();
    assertNotNull(rumbleValues.get());
    assertEquals(0f, rumbleValues.get()[0]);
  }
}
