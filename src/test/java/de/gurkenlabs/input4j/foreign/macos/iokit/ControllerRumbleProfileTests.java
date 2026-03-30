package de.gurkenlabs.input4j.foreign.macos.iokit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerRumbleProfileTests {

  @Test
  void testGenericProfile() {
    var profile = ControllerRumbleProfile.GENERIC;
    assertEquals((byte) 0x00, profile.getReportId());
    assertEquals(3, profile.getReportSize());
    assertEquals(1, profile.getLeftMotorOffset());
    assertEquals(2, profile.getRightMotorOffset());
  }

  @Test
  void testXboxOneProfile() {
    var profile = ControllerRumbleProfile.XBOX_ONE;
    assertEquals(0x09, profile.getReportId());
    assertEquals(6, profile.getReportSize());
    assertEquals(1, profile.getLeftMotorOffset());
    assertEquals(2, profile.getRightMotorOffset());
  }

  @Test
  void testDualShock4Profile() {
    var profile = ControllerRumbleProfile.DUALSHOCK_4;
    assertEquals(0x05, profile.getReportId());
    assertEquals(31, profile.getReportSize());
    assertEquals(4, profile.getLeftMotorOffset());
    assertEquals(5, profile.getRightMotorOffset());
  }

  @Test
  void testNintendoProControllerProfile() {
    var profile = ControllerRumbleProfile.NINTENDO_PRO_CONTROLLER;
    assertEquals(0x10, profile.getReportId());
    assertEquals(10, profile.getReportSize());
    assertEquals(5, profile.getLeftMotorOffset());
    assertEquals(9, profile.getRightMotorOffset());
  }

  @Test
  void test8BitDoGenericProfile() {
    var profile = ControllerRumbleProfile.EIGHTBITDO_GENERIC;
    assertEquals(0x00, profile.getReportId());
    assertEquals(3, profile.getReportSize());
    assertEquals(1, profile.getLeftMotorOffset());
    assertEquals(2, profile.getRightMotorOffset());
  }

  @Test
  void test8BitDoSN30ProPlusProfile() {
    var profile = ControllerRumbleProfile.EIGHTBITDO_SN30_PRO_PLUS;
    assertEquals(0x05, profile.getReportId());
    assertEquals(31, profile.getReportSize());
    assertEquals(4, profile.getLeftMotorOffset());
    assertEquals(5, profile.getRightMotorOffset());
  }

  @Test
  void testDualSenseProfile() {
    var profile = ControllerRumbleProfile.DUALSENSE;
    assertEquals(0x02, profile.getReportId());
    assertEquals(47, profile.getReportSize());
    assertEquals(4, profile.getLeftMotorOffset());
    assertEquals(5, profile.getRightMotorOffset());
  }

  @Test
  void testFromVendorProductXbox() {
    // Microsoft vendor ID
    var profile = ControllerRumbleProfile.fromVendorProduct(0x045E, 0x02DD);
    assertEquals(ControllerRumbleProfile.XBOX_ONE, profile);

    profile = ControllerRumbleProfile.fromVendorProduct(0x045E, 0x0B00);
    assertEquals(ControllerRumbleProfile.XBOX_SERIES_X, profile);
  }

  @Test
  void testFromVendorProductSony() {
    // Sony vendor ID
    var profile = ControllerRumbleProfile.fromVendorProduct(0x054C, 0x09CC);
    assertEquals(ControllerRumbleProfile.DUALSHOCK_4, profile);

    profile = ControllerRumbleProfile.fromVendorProduct(0x054C, 0x0CE6);
    assertEquals(ControllerRumbleProfile.DUALSENSE, profile);
  }

  @Test
  void testFromVendorProductNintendo() {
    // Nintendo vendor ID
    var profile = ControllerRumbleProfile.fromVendorProduct(0x057E, 0x2009);
    assertEquals(ControllerRumbleProfile.NINTENDO_PRO_CONTROLLER, profile);
  }

  @Test
  void testFromVendorProduct8BitDo() {
    // 8BitDo vendor ID
    var profile = ControllerRumbleProfile.fromVendorProduct(0x2DC8, 0x6002);
    assertEquals(ControllerRumbleProfile.EIGHTBITDO_SN30_PRO_PLUS, profile);

    // Unknown 8BitDo product defaults to generic
    profile = ControllerRumbleProfile.fromVendorProduct(0x2DC8, 0x3011);
    assertEquals(ControllerRumbleProfile.EIGHTBITDO_GENERIC, profile);
  }

  @Test
  void testFromVendorProductUnknown() {
    // Unknown vendor returns GENERIC
    var profile = ControllerRumbleProfile.fromVendorProduct(0x1234, 0x5678);
    assertEquals(ControllerRumbleProfile.GENERIC, profile);
  }

  @Test
  void testDefaultProfile() {
    assertEquals(ControllerRumbleProfile.GENERIC, ControllerRumbleProfile.defaultProfile());
  }
}
