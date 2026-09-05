package de.gurkenlabs.input4j.foreign.linux;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.lang.foreign.Arena;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class LinuxPermissionTests {
  @Test
  void testErrnoConstants() {
    assertEquals(9, Linux.EBADF, "EBADF should be 9");
    assertEquals(11, Linux.EAGAIN, "EAGAIN should be 11");
    assertEquals(13, Linux.EACCES, "EACCES should be 13");
    assertEquals(19, Linux.ENODEV, "ENODEV should be 19");
  }

  @Test
  void testOpenFlags() {
    assertEquals(0, Linux.O_RDONLY, "O_RDONLY should be 0");
    assertEquals(2, Linux.O_RDWR, "O_RDWR should be 2");
    assertEquals(0x800, Linux.O_NONBLOCK, "O_NONBLOCK should be 0x800");
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testOpenRdwrWithErrno() {
    try (var arena = Arena.ofShared()) {
      int[] outErrno = new int[1];
      int fd = Linux.openRdwr(arena, "/nonexistent/path/to/test", outErrno);

      assertEquals(Linux.ERROR, fd, "Opening nonexistent path should fail");
      assertEquals(Linux.ENOENT, outErrno[0], "Should get ENOENT for nonexistent path");
    }
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testOpenRdwrWithoutErrno() {
    try (var arena = Arena.ofShared()) {
      int fd = Linux.openRdwr(arena, "/nonexistent/path/to/test");

      assertEquals(Linux.ERROR, fd, "Opening nonexistent path should fail");
    }
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testLinuxEventDeviceOpenedReadOnlyField() {
    File dev = new File("/dev/input");
    File[] eventDevices = dev.listFiles((d, name) -> name.startsWith("event"));

    if (eventDevices == null || eventDevices.length == 0) {
      return;
    }

    try (var arena = Arena.ofShared()) {
      LinuxEventDevice device = new LinuxEventDevice(arena, eventDevices[0].getAbsolutePath(), true);

      if (device.fd != Linux.ERROR) {
        assertNotNull(device.name, "Device name should not be null for valid device");
        assertNotNull(device.id, "Device ID should not be null for valid device");
      }
    }
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testLinuxEventDeviceSupportsForceFeedbackDisabledWhenReadOnly() {
    try (var arena = Arena.ofShared()) {
      LinuxEventDevice device = new LinuxEventDevice(arena, "/nonexistent/device");

      // When device fails to open, supportsForceFeedback should be false
      assertEquals(Linux.ERROR, device.fd);
      assertFalse(device.supportsForceFeedback, "supportsForceFeedback should be false when device fails to open");
    }
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testLinuxEventDeviceForceRumbleFalseConstructor() {
    File dev = new File("/dev/input");
    File[] eventDevices = dev.listFiles((d, name) -> name.startsWith("event"));

    if (eventDevices == null || eventDevices.length == 0) {
      return;
    }

    try (var arena = Arena.ofShared()) {
      // forceRumble=false always opens read-only
      LinuxEventDevice device = new LinuxEventDevice(arena, eventDevices[0].getAbsolutePath(), false);

      if (device.fd != Linux.ERROR) {
        // When opened read-only (forceRumble=false), supportsForceFeedback should be false
        // even if the device has force feedback capability
        assertTrue(device.openedReadOnly, "openedReadOnly should be true when forceRumble=false");
        assertFalse(device.supportsForceFeedback, "supportsForceFeedback should be false when read-only");
      }
    }
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testReadWriteHandleReturnTypes() {
    var readHandle = Linux.getHandle(Linux.HANDLE_READ);
    var writeHandle = Linux.getHandle(Linux.HANDLE_WRITE);
    assertNotNull(readHandle, "HANDLE_READ downcall handle must be present");
    assertNotNull(writeHandle, "HANDLE_WRITE downcall handle must be present");

    boolean is32Bit = System.getProperty("os.arch", "").toLowerCase().matches(".*(arm|i[3-6]86|x86).*")
        && !System.getProperty("os.arch", "").toLowerCase().contains("64");
    Class<?> expectedReturnType = is32Bit ? int.class : long.class;

    assertEquals(expectedReturnType, readHandle.type().returnType(),
        "read downcall handle return type must match ssize_t for the architecture");
    assertEquals(expectedReturnType, writeHandle.type().returnType(),
        "write downcall handle return type must match ssize_t for the architecture");
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testLinuxEventDeviceIsDisconnected() {
    try (var arena = Arena.ofShared()) {
      LinuxEventDevice device = new LinuxEventDevice(arena, "/nonexistent/device");
      assertTrue(device.isDisconnected(), "Device with ERROR fd should be disconnected");
      device.close();
      assertTrue(device.isDisconnected(), "Closed device should be disconnected");
      assertDoesNotThrow(() -> device.close());
    }
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testLinuxEventDeviceOwnArenaLifecycle() {
    var arena = Arena.ofShared();
    LinuxEventDevice device = new LinuxEventDevice(arena, "/nonexistent/device");
    assertTrue(arena.scope().isAlive());
    device.close();
    assertFalse(arena.scope().isAlive(), "Closing LinuxEventDevice must close its own arena");
  }
}