package de.gurkenlabs.input4j.windows.dinput;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;

import static org.junit.jupiter.api.Assertions.*;

class NativeDataStructTests {
  final static GUID TEST_GUID = new GUID(0x00000001, (short) 0x0002, (short) 0x0003, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x10, (byte) 0x11);
  final static GUID TEST_GUID2 = new GUID(0x00000001, 0x0002, 0x0003, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x10, 0x11);

  @Test
  void testInitDirectInputPlugin() {
    assertDoesNotThrow(DirectInputPlugin::new);
  }

  @Test
  void testDirectInputInitDevices() {
    try (var plugin = new DirectInputPlugin()) {
      plugin.internalInitDevices();
    }
  }


  @Test
  void testGUID() {
    try (var memorySession = Arena.ofConfined()) {
      var segment = memorySession.allocate(GUID.$LAYOUT);
      TEST_GUID.write(segment);

      var testGuid = GUID.read(segment);
      assertEquals(TEST_GUID.Data1, testGuid.Data1);
      assertEquals(TEST_GUID.Data2, testGuid.Data2);
      assertEquals(TEST_GUID.Data3, testGuid.Data3);
      assertArrayEquals(TEST_GUID.Data4, testGuid.Data4);

      TEST_GUID2.write(segment);

      var testGuid2 = GUID.read(segment);
      assertEquals(TEST_GUID2.Data1, testGuid2.Data1);
      assertEquals(TEST_GUID2.Data2, testGuid2.Data2);
      assertEquals(TEST_GUID2.Data3, testGuid2.Data3);
      assertArrayEquals(TEST_GUID2.Data4, testGuid2.Data4);
    }
  }

  @Test
  void testDIOBJECTDATAFORMAT() {
    var objectDataFormat = new DIOBJECTDATAFORMAT();

    objectDataFormat.dwOfs = 1;
    objectDataFormat.dwType = 4;
    objectDataFormat.dwFlags = 2;

    try (var memorySession = Arena.ofConfined()) {
      objectDataFormat.pguid = memorySession.allocate(GUID.$LAYOUT);
      var segment = memorySession.allocate(DIOBJECTDATAFORMAT.$LAYOUT);
      objectDataFormat.write(segment);

      var testobjectDataFormat = DIOBJECTDATAFORMAT.read(segment);
      assertEquals(objectDataFormat.pguid, testobjectDataFormat.pguid);
      assertEquals(objectDataFormat.dwOfs, testobjectDataFormat.dwOfs);
      assertEquals(objectDataFormat.dwType, testobjectDataFormat.dwType);
      assertEquals(objectDataFormat.dwFlags, testobjectDataFormat.dwFlags);
    }
  }

  @Test
  void testDIDATAFORMAT() {
    final GUID FORMAT_GUID1 = new GUID(0x11111111, 0x1111, 0x1111, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11);
    final GUID FORMAT_GUID2 = new GUID(0x22222222, 0x2222, 0x2222, 0x22, 0x22, 0x22, 0x22, 0x22, 0x22, 0x22, 0x22);
    final GUID FORMAT_GUID3 = new GUID(0x33333333, 0x3333, 0x3333, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33);

    try (var memoryArena = Arena.ofConfined()) {
      var guidsSegment = memoryArena.allocate(MemoryLayout.sequenceLayout(3, GUID.$LAYOUT));
      FORMAT_GUID1.write(guidsSegment);

      var secondSegment = guidsSegment.asSlice(GUID.$LAYOUT.byteSize());
      FORMAT_GUID2.write(secondSegment);

      var thirdSegment = guidsSegment.asSlice(GUID.$LAYOUT.byteSize());
      FORMAT_GUID3.write(thirdSegment);

      var objectDataFormat1 = new DIOBJECTDATAFORMAT(guidsSegment, 1, 1, 1);
      var objectDataFormat2 = new DIOBJECTDATAFORMAT(secondSegment, 2, 2, 2);
      var objectDataFormat3 = new DIOBJECTDATAFORMAT(thirdSegment, 3, 3, 3);
      var dataFormat = new DIDATAFORMAT();
      dataFormat.dwFlags = IDirectInputDevice8.DIDF_ABSAXIS;
      dataFormat.dwNumObjs = 3;
      dataFormat.dwDataSize = dataFormat.dwNumObjs * 4;
      dataFormat.setObjectDataFormats(new DIOBJECTDATAFORMAT[]{objectDataFormat1, objectDataFormat2, objectDataFormat3});


      var segment = memoryArena.allocate(DIDATAFORMAT.$LAYOUT);
      dataFormat.write(segment, memoryArena);

      var testDataFormat = DIDATAFORMAT.read(segment, memoryArena);
      assertEquals(dataFormat.dwSize, testDataFormat.dwSize);
      assertEquals(dataFormat.dwObjSize, testDataFormat.dwObjSize);
      assertEquals(dataFormat.dwFlags, testDataFormat.dwFlags);
      assertEquals(dataFormat.dwNumObjs, testDataFormat.dwNumObjs);
      assertEquals(dataFormat.dwDataSize, testDataFormat.dwDataSize);
      assertEquals(dataFormat.rgodf, testDataFormat.rgodf);
      assertArrayEquals(dataFormat.getObjectDataFormats(), testDataFormat.getObjectDataFormats());
    }

  }
}
