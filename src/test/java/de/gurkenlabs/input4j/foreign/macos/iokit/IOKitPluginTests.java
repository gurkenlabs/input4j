package de.gurkenlabs.input4j.foreign.macos.iokit;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IOKitPluginTests {
  @Test
  void testNormalizeInputValue() {
    IOHIDElement element = new IOHIDElement();
    element.min = 0;
    element.max = 100;
    element.usage = IOHIDElementUsage.X;
    element.type = IOHIDElementType.AXIS;

    // Test normalization for axis
    assertEquals(0.0f, IOKitPlugin.normalizeInputValue(50, element, true), 0.01);
    assertEquals(0.0f, IOKitPlugin.normalizeInputValue(0, element, true), 0.01);
    assertEquals(-0.98f, IOKitPlugin.normalizeInputValue(1, element, true), 0.01);
    assertEquals(1.0f, IOKitPlugin.normalizeInputValue(100, element, true), 0.01);

    // Test normalization for button
    element.type = IOHIDElementType.BUTTON;
    assertEquals(0.0f, IOKitPlugin.normalizeInputValue(0, element, false), 0.01);
    assertEquals(1.0f, IOKitPlugin.normalizeInputValue(1, element, false), 0.01);
    assertEquals(1.0f, IOKitPlugin.normalizeInputValue(100, element, false), 0.01);
  }

  @Test
  void testRumbleConstants() {
    // Test that the rumble report type constants are defined correctly
    // kIOHIDReportTypeOutput = 1 for sending rumble/haptic feedback
    assertEquals(1, MacOS.kIOHIDReportTypeOutput);

    // kIOHIDReportTypeInput = 0 for receiving input
    assertEquals(0, MacOS.kIOHIDReportTypeInput);

    // kIOHIDReportTypeFeature = 2 for feature reports
    assertEquals(2, MacOS.kIOHIDReportTypeFeature);
  }

  @Test
  void testIntensityToBytes() {
    // Test the actual implementation method
    // Edge cases
    int[] result = IOKitPlugin.intensityToBytes(0.0f, 0.0f);
    assertEquals(0, result[0]);
    assertEquals(0, result[1]);

    result = IOKitPlugin.intensityToBytes(1.0f, 1.0f);
    assertEquals(255, result[0]);
    assertEquals(255, result[1]);

    // Test mid-range values
    result = IOKitPlugin.intensityToBytes(0.5f, 0.5f);
    assertEquals(127, result[0]);
    assertEquals(127, result[1]);

    // Test values above 1.0 are clamped
    result = IOKitPlugin.intensityToBytes(1.5f, 1.5f);
    assertEquals(255, result[0]);
    assertEquals(255, result[1]);

    // Test negative values are clamped
    result = IOKitPlugin.intensityToBytes(-0.5f, -0.5f);
    assertEquals(0, result[0]);
    assertEquals(0, result[1]);

    // Test asymmetric (left vs right)
    result = IOKitPlugin.intensityToBytes(0.8f, 0.2f);
    assertEquals(204, result[0]);
    assertEquals(51, result[1]);
  }

  @Test
  void testShouldStopRumble() {
    // Test the actual implementation method
    // Null or empty should stop
    assertTrue(IOKitPlugin.shouldStopRumble(null));
    assertTrue(IOKitPlugin.shouldStopRumble(new float[0]));

    // Values below threshold should stop
    assertTrue(IOKitPlugin.shouldStopRumble(new float[] {0.009f}));
    assertTrue(IOKitPlugin.shouldStopRumble(new float[] {0.005f, 0.005f}));

    // Values at or above threshold should NOT stop
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.01f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.5f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.5f, 0.0f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.5f, 0.005f}));

    // When left is below threshold, stop regardless of right (right is never checked)
    // Note: both must be below threshold to stop when there are 2 values
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.005f, 0.5f}));
  }

  @Test
  void testRumbleThreshold() {
    // Test the threshold behavior through the actual implementation
    // Values below threshold should trigger stop rumble
    assertTrue(IOKitPlugin.shouldStopRumble(new float[] {0.005f}));
    assertTrue(IOKitPlugin.shouldStopRumble(new float[] {0.009f}));

    // Values at or above threshold should NOT trigger stop
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.01f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {0.5f}));
    assertFalse(IOKitPlugin.shouldStopRumble(new float[] {1.0f}));
  }

  // --- populateDevices + cookie-based lookup tests -------------------------

  private static IOHIDElement makeElement(int cookie, long address, IOHIDElementUsage usage, IOHIDElementType type) {
    var element = new IOHIDElement();
    element.cookie = cookie;
    element.address = address;
    element.usage = usage;
    element.type = type;
    element.name = "element-" + cookie;
    return element;
  }

  private static IOHIDDevice makeDevice(long address, int vendorId, int productId, IOHIDElement... elements) {
    var device = new IOHIDDevice();
    device.address = address;
    device.vendorId = vendorId;
    device.productId = productId;
    device.productName = "Device " + address;
    device.manufacturer = "TestCo";
    device.transport = "USB";
    device.usage = 0x05; // gamepad
    device.usagePage = 0x01; // generic desktop
    for (var element : elements) {
      device.addElement(element);
    }
    return device;
  }

  @Test
  void testPopulateDevices_registersDevicesAndComponents() {
    var plugin = new IOKitPlugin();
    var button = makeElement(0x10, 0xAAAA0001L, IOHIDElementUsage.BUTTON_1, IOHIDElementType.BUTTON);
    var axis = makeElement(0x20, 0xAAAA0002L, IOHIDElementUsage.X, IOHIDElementType.AXIS);
    var device = makeDevice(0x1234L, 0x046D, 0xC21D, button, axis);

    plugin.populateDevices(List.of(device));

    var all = plugin.getAll();
    assertEquals(1, all.size());
    InputDevice registered = all.iterator().next();
    assertEquals("4660", registered.getID());
    assertEquals(0x046D, registered.getVendorId());
    assertEquals(0xC21D, registered.getProductId());
    // Two non-UNDEFINED elements get a component each.
    assertEquals(2, registered.getComponents().size());
  }

  @Test
  void testPopulateDevices_skipsUndefinedElements() {
    var plugin = new IOKitPlugin();
    var defined = makeElement(0x10, 0x1L, IOHIDElementUsage.BUTTON_1, IOHIDElementType.BUTTON);
    var undefined = makeElement(0x11, 0x2L, IOHIDElementUsage.UNDEFINED, IOHIDElementType.MISC);
    var device = makeDevice(0xCAFE, 0x1234, 0x5678, defined, undefined);

    plugin.populateDevices(List.of(device));

    // Even though the UNDEFINED element is skipped for components, it must
    // still be added to the cookie index so the input value callback can
    // resolve values that arrive against it.
    InputDevice registered = plugin.getAll().iterator().next();
    assertEquals(1, registered.getComponents().size());
    assertNotNull(plugin.findElement(0xCAFEL, 0x10));
    assertNotNull(plugin.findElement(0xCAFEL, 0x11));
  }

  @Test
  void testFindElement_resolvesByCookieAndDevice() {
    var plugin = new IOKitPlugin();
    var button = makeElement(0x42, 0xDEADBEEFL, IOHIDElementUsage.BUTTON_1, IOHIDElementType.BUTTON);
    var device = makeDevice(0x9999L, 0x046D, 0xC21D, button);
    plugin.populateDevices(List.of(device));

    var found = plugin.findElement(0x9999L, 0x42);
    assertNotNull(found);
    assertEquals(0x42, found.cookie);
  }

  @Test
  void testFindElement_returnsNullForUnknownCookie() {
    var plugin = new IOKitPlugin();
    var button = makeElement(0x42, 0xDEADBEEFL, IOHIDElementUsage.BUTTON_1, IOHIDElementType.BUTTON);
    var device = makeDevice(0x9999L, 0x046D, 0xC21D, button);
    plugin.populateDevices(List.of(device));

    assertNull(plugin.findElement(0x9999L, 0x99));
  }

  @Test
  void testFindElement_returnsNullForUnknownDevice() {
    var plugin = new IOKitPlugin();
    var button = makeElement(0x42, 0xDEADBEEFL, IOHIDElementUsage.BUTTON_1, IOHIDElementType.BUTTON);
    var device = makeDevice(0x9999L, 0x046D, 0xC21D, button);
    plugin.populateDevices(List.of(device));

    assertNull(plugin.findElement(0xDEADL, 0x42));
  }

  @Test
  void testFindElement_isolatesCookiesAcrossDevices() {
    var plugin = new IOKitPlugin();
    // Two devices, each with an element that happens to share a cookie
    // value. The lookup must distinguish them by device address.
    var leftButton = makeElement(0x10, 0x1L, IOHIDElementUsage.BUTTON_1, IOHIDElementType.BUTTON);
    var rightButton = makeElement(0x10, 0x2L, IOHIDElementUsage.BUTTON_1, IOHIDElementType.BUTTON);
    var deviceA = makeDevice(0xAA00L, 0x1, 0x1, leftButton);
    var deviceB = makeDevice(0xBB00L, 0x2, 0x2, rightButton);
    plugin.populateDevices(List.of(deviceA, deviceB));

    var resolvedA = plugin.findElement(0xAA00L, 0x10);
    var resolvedB = plugin.findElement(0xBB00L, 0x10);
    assertNotNull(resolvedA);
    assertNotNull(resolvedB);
    // Different underlying IOHIDElement instances.
    assertNotSame(resolvedA, resolvedB);
  }

  @Test
  void testPopulateDevices_clearsCookieIndexOnClose() throws Exception {
    var plugin = new IOKitPlugin();
    var button = makeElement(0x42, 0xDEADBEEFL, IOHIDElementUsage.BUTTON_1, IOHIDElementType.BUTTON);
    var device = makeDevice(0x9999L, 0x046D, 0xC21D, button);
    plugin.populateDevices(List.of(device));
    assertNotNull(plugin.findElement(0x9999L, 0x42));

    plugin.close();

    assertNull(plugin.findElement(0x9999L, 0x42));
    // nativeDevices cleared too.
    Field nativeDevicesField = IOKitPlugin.class.getDeclaredField("nativeDevices");
    nativeDevicesField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, IOHIDDevice> nativeDevices = (Map<String, IOHIDDevice>) nativeDevicesField.get(plugin);
    assertTrue(nativeDevices.isEmpty());
  }
}
