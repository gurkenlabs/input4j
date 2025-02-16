package de.gurkenlabs.input4j.foreign.linux;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LinuxDataStructTests {
  @Test
  @EnabledOnOs(OS.LINUX)
  void testInitLinuxEventDevicePlugin() {
    assertDoesNotThrow(LinuxEventDevicePlugin::new);
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testLinuxEventDevicesInit() {
    try (var plugin = new LinuxEventDevicePlugin()) {
      plugin.internalInitDevices(null);
    }
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void ensureCapturedStateContainsErrNo() {
    var capturedNames = Linker.Option.captureStateLayout()
            .memberLayouts()
            .stream()
            .map(MemoryLayout::name)
            .flatMap(Optional::stream)
            .toList();

    assertNotNull(capturedNames);
    assertTrue(capturedNames.contains("errno"));
  }

  @Test
  public void testInputAbsInfo() {
    try (var memorySession = Arena.ofConfined()) {
      var absInfo = new input_absinfo();
      absInfo.value = 123;
      absInfo.minimum = -1000;
      absInfo.maximum = 1000;
      absInfo.fuzz = 10;
      absInfo.flat = 5;
      absInfo.resolution = 1;

      var segment = memorySession.allocate(input_absinfo.$LAYOUT);
      absInfo.write(segment);

      var absInfoFromMemory = input_absinfo.read(segment);
      assertEquals(absInfo.value, absInfoFromMemory.value);
      assertEquals(absInfo.minimum, absInfoFromMemory.minimum);
      assertEquals(absInfo.maximum, absInfoFromMemory.maximum);
      assertEquals(absInfo.fuzz, absInfoFromMemory.fuzz);
      assertEquals(absInfo.flat, absInfoFromMemory.flat);
      assertEquals(absInfo.resolution, absInfoFromMemory.resolution);
    }
  }

  @Test
  public void testInputId() {
    try (var memorySession = Arena.ofConfined()) {
      var inputId = new input_id();
      inputId.bustype = 100;
      inputId.vendor = 200;
      inputId.product = 300;
      inputId.version = 400;

      var segment = memorySession.allocate(input_id.$LAYOUT);
      inputId.write(segment);

      var inputIdFromMemory = input_id.read(segment);
      assertEquals(inputId.bustype, inputIdFromMemory.bustype);
      assertEquals(inputId.vendor, inputIdFromMemory.vendor);
      assertEquals(inputId.product, inputIdFromMemory.product);
      assertEquals(inputId.version, inputIdFromMemory.version);
    }
  }

  @Test
  public void testInputEvent() {
    try (var memorySession = Arena.ofConfined()) {
      var inputEvent = new input_event();
      inputEvent.time = new timeval();
      inputEvent.time.tv_sec = 999;
      inputEvent.time.tv_usec = 100001;
      inputEvent.type = 123;
      inputEvent.code = 101;
      inputEvent.value = 1111;

      var segment = memorySession.allocate(input_event.$LAYOUT);
      inputEvent.write(segment);

      var inputEventFromMemory = input_event.read(segment);
      assertEquals(inputEvent.time.tv_sec, inputEventFromMemory.time.tv_sec);
      assertEquals(inputEvent.time.tv_usec, inputEventFromMemory.time.tv_usec);
      assertEquals(inputEvent.type, inputEventFromMemory.type);
      assertEquals(inputEvent.code, inputEventFromMemory.code);
      assertEquals(inputEvent.value, inputEventFromMemory.value);
    }
  }

  @Test
  public void testTimeval() {
    try (var memorySession = Arena.ofConfined()) {
      var t = new timeval();
      t.tv_sec = 123456789L;
      t.tv_usec = 987654321L;

      var segment = memorySession.allocate(timeval.$LAYOUT);
      t.write(segment);

      var timevalFromMemory = timeval.read(segment);
      assertEquals(t.tv_sec, timevalFromMemory.tv_sec);
      assertEquals(t.tv_usec, timevalFromMemory.tv_usec);
    }
  }
}
