package de.gurkenlabs.input4j;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BatteryInfoTests {

  @Test
  void testFromPercentageFull() {
    BatteryInfo info = BatteryInfo.fromPercentage(BatteryType.ALKALINE, false, 100);
    assertEquals(BatteryLevel.FULL, info.level());
    assertEquals(100, info.getPercentage());
  }

  @Test
  void testFromPercentageHigh() {
    BatteryInfo info = BatteryInfo.fromPercentage(BatteryType.NIMH, false, 75);
    assertEquals(BatteryLevel.FULL, info.level());
    assertEquals(100, info.getPercentage());
  }

  @Test
  void testFromPercentageMedium() {
    BatteryInfo info = BatteryInfo.fromPercentage(BatteryType.UNKNOWN, false, 50);
    assertEquals(BatteryLevel.MEDIUM, info.level());
    assertEquals(50, info.getPercentage());
  }

  @Test
  void testFromPercentageLow() {
    BatteryInfo info = BatteryInfo.fromPercentage(BatteryType.UNKNOWN, false, 25);
    assertEquals(BatteryLevel.LOW, info.level());
    assertEquals(25, info.getPercentage());
  }

  @Test
  void testFromPercentageEmpty() {
    BatteryInfo info = BatteryInfo.fromPercentage(BatteryType.UNKNOWN, false, 10);
    assertEquals(BatteryLevel.EMPTY, info.level());
    assertEquals(0, info.getPercentage());
  }

  @Test
  void testFromPercentageInvalidNegative() {
    BatteryInfo info = BatteryInfo.fromPercentage(BatteryType.UNKNOWN, false, -1);
    assertEquals(BatteryLevel.UNKNOWN, info.level());
    assertEquals(-1, info.getPercentage());
  }

  @Test
  void testFromPercentageInvalidOver100() {
    BatteryInfo info = BatteryInfo.fromPercentage(BatteryType.UNKNOWN, false, 101);
    assertEquals(BatteryLevel.UNKNOWN, info.level());
    assertEquals(-1, info.getPercentage());
  }

  @Test
  void testDefaultConstructor() {
    BatteryInfo info = new BatteryInfo(BatteryType.WIRED, BatteryLevel.FULL, false);
    assertEquals(BatteryType.WIRED, info.type());
    assertEquals(BatteryLevel.FULL, info.level());
    assertFalse(info.charging());
    assertEquals(-1, info.getPercentage());
  }

  @Test
  void testIsAvailable() {
    assertTrue(new BatteryInfo(BatteryType.ALKALINE, BatteryLevel.FULL, false).isAvailable());
    assertTrue(new BatteryInfo(BatteryType.NIMH, BatteryLevel.MEDIUM, false).isAvailable());
    assertTrue(new BatteryInfo(BatteryType.UNKNOWN, BatteryLevel.LOW, false).isAvailable());
    assertTrue(new BatteryInfo(BatteryType.WIRED, BatteryLevel.FULL, false).isAvailable());
  }

  @Test
  void testIsNotAvailableDisconnected() {
    assertFalse(new BatteryInfo(BatteryType.DISCONNECTED, BatteryLevel.UNKNOWN, false).isAvailable());
  }

  @Test
  void testIsNotAvailableUnknownLevel() {
    assertFalse(new BatteryInfo(BatteryType.UNKNOWN, BatteryLevel.UNKNOWN, false).isAvailable());
  }

  @Test
  void testGetPercentageWired() {
    BatteryInfo info = new BatteryInfo(BatteryType.WIRED, BatteryLevel.FULL, false);
    assertEquals(-1, info.getPercentage());
  }

  @Test
  void testGetPercentageDisconnected() {
    BatteryInfo info = new BatteryInfo(BatteryType.DISCONNECTED, BatteryLevel.EMPTY, false);
    assertEquals(-1, info.getPercentage());
  }

  @Test
  void testGetPercentageUnknownLevel() {
    BatteryInfo info = new BatteryInfo(BatteryType.UNKNOWN, BatteryLevel.UNKNOWN, false);
    assertEquals(-1, info.getPercentage());
  }

  @Test
  void testChargingFlag() {
    BatteryInfo notCharging = BatteryInfo.fromPercentage(BatteryType.ALKALINE, false, 50);
    BatteryInfo charging = BatteryInfo.fromPercentage(BatteryType.NIMH, true, 75);
    assertFalse(notCharging.charging());
    assertTrue(charging.charging());
  }
}