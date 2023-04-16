package de.gurkenlabs.litiengine.input.windows;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NativeDataStructs {
  final static GUID TEST_GUID = new GUID(0x00000001, (short) 0x0002, (short) 0x0003, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x10, (byte) 0x11);

  @Test
  void testGUID() {
    try (var memorySession = Arena.openConfined()) {
      var segment = memorySession.allocate(GUID.$LAYOUT);
      TEST_GUID.write(segment);

      var testGuid = GUID.read(segment);
      assertEquals(TEST_GUID.Data1, testGuid.Data1);
      assertEquals(TEST_GUID.Data2, testGuid.Data2);
      assertEquals(TEST_GUID.Data3, testGuid.Data3);
      assertArrayEquals(TEST_GUID.Data4, testGuid.Data4);
    }
  }

  @Test
  void testDIOBJECTDATAFORMAT() {
    var objectDataFormat = new DIOBJECTDATAFORMAT();

    objectDataFormat.dwOfs = 1;
    objectDataFormat.dwType = 4;
    objectDataFormat.dwFlags = 2;

    try (var memorySession = Arena.openConfined()) {
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
    final GUID FORMAT_GUID1 = new GUID(0x11111111, (short) 0x1111, (short) 0x1111, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11);
    final GUID FORMAT_GUID2 = new GUID(0x22222222, (short) 0x2222, (short) 0x2222, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22);
    final GUID FORMAT_GUID3 = new GUID(0x33333333, (short) 0x3333, (short) 0x3333, (byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x33);

    try (var memoryArea = Arena.openConfined()) {
      var guidsSegment = memoryArea.allocate(MemoryLayout.sequenceLayout(3, GUID.$LAYOUT));
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


      var segment = memoryArea.allocate(DIDATAFORMAT.$LAYOUT);
      dataFormat.write(segment, memoryArea);

      var testDataFormat = DIDATAFORMAT.read(segment);
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
