package de.gurkenlabs.input4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerDatabaseTests {

  @BeforeEach
  public void setUp() {
    ControllerDatabase.clearCustom();
  }

  @AfterEach
  public void tearDown() {
    ControllerDatabase.clearCustom();
  }

  @Test
  void testLookupKnownController_Xbox() {
    var result = ControllerDatabase.lookup(ControllerDatabase.VENDOR_MICROSOFT, 0x028E);
    assertTrue(result.isPresent());
    assertEquals("Xbox 360 Controller", result.get().displayName());
    assertEquals(ControllerType.XBOX, result.get().type());
  }

  @Test
  void testLookupKnownController_DualSense() {
    var result = ControllerDatabase.lookup(ControllerDatabase.VENDOR_SONY, 0x0CE6);
    assertTrue(result.isPresent());
    assertEquals("DualSense", result.get().displayName());
    assertEquals(ControllerType.PLAYSTATION, result.get().type());
  }

  @Test
  void testLookupKnownController_SwitchPro() {
    var result = ControllerDatabase.lookup(ControllerDatabase.VENDOR_NINTENDO, 0x2009);
    assertTrue(result.isPresent());
    assertEquals("Nintendo Switch Pro Controller", result.get().displayName());
    assertEquals(ControllerType.NINTENDO, result.get().type());
  }

  @Test
  void testLookupKnownController_8BitDo() {
    var result = ControllerDatabase.lookup(ControllerDatabase.VENDOR_8BITDO, 0x6001);
    assertTrue(result.isPresent());
    assertEquals("8BitDo SN30 Pro+", result.get().displayName());
    assertEquals(ControllerType.EIGHTBITDO, result.get().type());
  }

  @Test
  void testLookupUnknownReturnsEmpty() {
    var result = ControllerDatabase.lookup(0x1234, 0x5678);
    assertTrue(result.isEmpty());
  }

  @Test
  void testLookupWithNegativeIdsReturnsEmpty() {
    assertTrue(ControllerDatabase.lookup(-1, -1).isEmpty());
    assertTrue(ControllerDatabase.lookup(0x045E, -1).isEmpty());
    assertTrue(ControllerDatabase.lookup(-1, 0x028E).isEmpty());
  }

  @Test
  void testGetControllerType_Xbox() {
    var type = ControllerDatabase.getControllerType(ControllerDatabase.VENDOR_MICROSOFT, 0x028E);
    assertEquals(ControllerType.XBOX, type);
  }

  @Test
  void testGetControllerType_PlayStation() {
    var type = ControllerDatabase.getControllerType(ControllerDatabase.VENDOR_SONY, 0x0CE6);
    assertEquals(ControllerType.PLAYSTATION, type);
  }

  @Test
  void testGetControllerType_Nintendo() {
    var type = ControllerDatabase.getControllerType(ControllerDatabase.VENDOR_NINTENDO, 0x2009);
    assertEquals(ControllerType.NINTENDO, type);
  }

  @Test
  void testGetControllerType_8BitDo() {
    var type = ControllerDatabase.getControllerType(ControllerDatabase.VENDOR_8BITDO, 0x6001);
    assertEquals(ControllerType.EIGHTBITDO, type);
  }

  @Test
  void testGetControllerType_Generic() {
    var type = ControllerDatabase.getControllerType(0xFFFF, 0xFFFF);
    assertEquals(ControllerType.GENERIC, type);
  }

  @Test
  void testGetDisplayName_ReturnsName() {
    var name = ControllerDatabase.getDisplayName(ControllerDatabase.VENDOR_SONY, 0x0CE6);
    assertEquals("DualSense", name);
  }

  @Test
  void testGetDisplayName_ReturnsNull() {
    var name = ControllerDatabase.getDisplayName(0x1234, 0x5678);
    assertNull(name);
  }

  @Test
  void testRegister_CustomController() {
    ControllerDatabase.register(0x9999, 0x1111, "Custom Controller", ControllerType.GENERIC);
    var result = ControllerDatabase.lookup(0x9999, 0x1111);
    assertTrue(result.isPresent());
    assertEquals("Custom Controller", result.get().displayName());
    assertEquals(ControllerType.GENERIC, result.get().type());
  }

  @Test
  void testRegister_PrecedenceOverBuiltIn() {
    ControllerDatabase.register(ControllerDatabase.VENDOR_MICROSOFT, 0x028E, "Modified Controller", ControllerType.GENERIC);
    var result = ControllerDatabase.lookup(ControllerDatabase.VENDOR_MICROSOFT, 0x028E);
    assertTrue(result.isPresent());
    assertEquals("Modified Controller", result.get().displayName());
  }

  @Test
  void testUnregister_RemovesCustom() {
    ControllerDatabase.register(0x9999, 0x1111, "Custom Controller", ControllerType.GENERIC);
    ControllerDatabase.unregister(0x9999, 0x1111);
    var result = ControllerDatabase.lookup(0x9999, 0x1111);
    assertTrue(result.isEmpty());
  }

  @Test
  void testClearCustom_RemovesAll() {
    ControllerDatabase.register(0x9999, 0x1111, "Custom 1", ControllerType.GENERIC);
    ControllerDatabase.register(0x9999, 0x2222, "Custom 2", ControllerType.GENERIC);
    ControllerDatabase.clearCustom();
    assertTrue(ControllerDatabase.lookup(0x9999, 0x1111).isEmpty());
    assertTrue(ControllerDatabase.lookup(0x9999, 0x2222).isEmpty());
  }

  @Test
  void testRegister_InvalidArguments_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> ControllerDatabase.register(-1, 0x1111, "Test", ControllerType.GENERIC));
    assertThrows(IllegalArgumentException.class, () -> ControllerDatabase.register(0x9999, -1, "Test", ControllerType.GENERIC));
    assertThrows(IllegalArgumentException.class, () -> ControllerDatabase.register(0x9999, 0x1111, null, ControllerType.GENERIC));
    assertThrows(IllegalArgumentException.class, () -> ControllerDatabase.register(0x9999, 0x1111, "Test", null));
  }
}