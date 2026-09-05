package de.gurkenlabs.input4j;

import org.junit.jupiter.api.Test;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractInputDevicePluginTests {

  private static class TestPlugin extends AbstractInputDevicePlugin {
    List<InputDevice> currentDevices = new ArrayList<>();

    @Override
    public void internalInitDevices(Frame owner) {
      this.setDevices(new ArrayList<>(currentDevices));
    }

    @Override
    protected Collection<InputDevice> refreshInputDevices() {
      return new ArrayList<>(currentDevices);
    }
  }

  @Test
  void testGetAllThrowsWhenNotInitialized() {
    var plugin = new TestPlugin();
    assertThrows(IllegalStateException.class, plugin::getAll);
  }

  @Test
  void testDeviceConnectedListenerFires() {
    var plugin = new TestPlugin();
    var dev1 = new InputDevice("1", "Gamepad 1", "Gamepad", _ -> new float[0], (_, _) -> {});

    plugin.currentDevices.add(dev1);
    plugin.internalInitDevices(null);

    var connectedRef = new AtomicReference<InputDevice>();
    var changedCount = new AtomicInteger();
    plugin.onDeviceConnected(connectedRef::set);
    plugin.onDevicesChanged(changedCount::incrementAndGet);

    var dev2 = new InputDevice("2", "Gamepad 2", "Gamepad", _ -> new float[0], (_, _) -> {});
    plugin.currentDevices.add(dev2);

    // Force refresh
    plugin.refreshDevices(true);

    assertNotNull(connectedRef.get());
    assertEquals("2", connectedRef.get().getID());
    assertEquals(1, changedCount.get());
    assertEquals(2, plugin.getAll().size());
  }

  @Test
  void testDeviceDisconnectedListenerFires() {
    var plugin = new TestPlugin();
    var dev1 = new InputDevice("1", "Gamepad 1", "Gamepad", _ -> new float[0], (_, _) -> {});
    var dev2 = new InputDevice("2", "Gamepad 2", "Gamepad", _ -> new float[0], (_, _) -> {});

    plugin.currentDevices.add(dev1);
    plugin.currentDevices.add(dev2);
    plugin.internalInitDevices(null);

    var disconnectedRef = new AtomicReference<InputDevice>();
    var changedCount = new AtomicInteger();
    plugin.onDeviceDisconnected(disconnectedRef::set);
    plugin.onDevicesChanged(changedCount::incrementAndGet);

    plugin.currentDevices.remove(dev2);

    // Force refresh
    plugin.refreshDevices(true);

    assertNotNull(disconnectedRef.get());
    assertEquals("2", disconnectedRef.get().getID());
    assertEquals(1, changedCount.get());
    assertEquals(1, plugin.getAll().size());
  }

  @Test
  void testGetAllTriggersHotplugRefresh() throws InterruptedException {
    // Set a very short hotplug interval for this test
    int originalInterval = InputDevices.configure().getHotPlugInterval();
    try {
      InputDevices.configure().setHotPlugInterval(50);
      var plugin = new TestPlugin();
      plugin.internalInitDevices(null);
      assertEquals(0, plugin.getAll().size());

      var dev1 = new InputDevice("1", "Gamepad 1", "Gamepad", _ -> new float[0], (_, _) -> {});
      plugin.currentDevices.add(dev1);

      // Immediately calling getAll() within interval should not refresh yet
      assertEquals(0, plugin.getAll().size());

      // Wait for interval to elapse
      Thread.sleep(60);

      // Calling getAll() after interval should discover new device
      assertEquals(1, plugin.getAll().size());
    } finally {
      InputDevices.configure().setHotPlugInterval(originalInterval);
    }
  }

  @Test
  void testCloseClearsListeners() {
    var plugin = new TestPlugin();
    var dev1 = new InputDevice("1", "Gamepad 1", "Gamepad", _ -> new float[0], (_, _) -> {});
    plugin.currentDevices.add(dev1);
    plugin.internalInitDevices(null);

    var listenerFired = new AtomicBoolean(false);
    plugin.onDeviceConnected(_ -> listenerFired.set(true));
    plugin.onDeviceDisconnected(_ -> listenerFired.set(true));
    plugin.onDevicesChanged(() -> listenerFired.set(true));

    plugin.close();

    plugin.currentDevices.clear();
    plugin.refreshDevices(true);

    assertFalse(listenerFired.get());
  }
}
