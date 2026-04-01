package de.gurkenlabs.input4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTypeTests {

  @Test
  void testGetDisplayName_Xbox() {
    assertEquals("Xbox Controller", ControllerType.XBOX.getDisplayName());
  }

  @Test
  void testGetDisplayName_PlayStation() {
    assertEquals("PlayStation Controller", ControllerType.PLAYSTATION.getDisplayName());
  }

  @Test
  void testGetDisplayName_Nintendo() {
    assertEquals("Nintendo Controller", ControllerType.NINTENDO.getDisplayName());
  }

  @Test
  void testGetDisplayName_8BitDo() {
    assertEquals("8BitDo Controller", ControllerType.EIGHTBITDO.getDisplayName());
  }

  @Test
  void testGetDisplayName_Generic() {
    assertEquals("Game Controller", ControllerType.GENERIC.getDisplayName());
  }

  @Test
  void testFromVendorProduct_Microsoft() {
    var type = ControllerType.fromVendorProduct(ControllerDatabase.VENDOR_MICROSOFT, 0x028E);
    assertEquals(ControllerType.XBOX, type);
  }

  @Test
  void testFromVendorProduct_Sony() {
    var type = ControllerType.fromVendorProduct(ControllerDatabase.VENDOR_SONY, 0x05C4);
    assertEquals(ControllerType.PLAYSTATION, type);
  }

  @Test
  void testFromVendorProduct_Nintendo() {
    var type = ControllerType.fromVendorProduct(ControllerDatabase.VENDOR_NINTENDO, 0x2009);
    assertEquals(ControllerType.NINTENDO, type);
  }

  @Test
  void testFromVendorProduct_8BitDo() {
    var type = ControllerType.fromVendorProduct(ControllerDatabase.VENDOR_8BITDO, 0x6000);
    assertEquals(ControllerType.EIGHTBITDO, type);
  }

  @Test
  void testFromVendorProduct_UnknownVendor() {
    var type = ControllerType.fromVendorProduct(0x1234, 0x5678);
    assertEquals(ControllerType.GENERIC, type);
  }

  @Test
  void testFromVendorProduct_NegativeVendorId() {
    var type = ControllerType.fromVendorProduct(-1, 0x028E);
    assertEquals(ControllerType.GENERIC, type);
  }

  @Test
  void testFromVendorProduct_NegativeProductId() {
    var type = ControllerType.fromVendorProduct(ControllerDatabase.VENDOR_MICROSOFT, -1);
    assertEquals(ControllerType.GENERIC, type);
  }

  @Test
  void testFromVendorProduct_BothNegative() {
    var type = ControllerType.fromVendorProduct(-1, -1);
    assertEquals(ControllerType.GENERIC, type);
  }
}