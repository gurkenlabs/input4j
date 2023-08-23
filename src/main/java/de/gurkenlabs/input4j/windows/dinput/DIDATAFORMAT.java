package de.gurkenlabs.input4j.windows.dinput;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

class DIDATAFORMAT {
  public int dwSize = (int) DIDATAFORMAT.$LAYOUT.byteSize();
  public int dwObjSize = (int) DIOBJECTDATAFORMAT.$LAYOUT.byteSize();
  public int dwFlags;
  public int dwDataSize;
  public int dwNumObjs;

  public MemorySegment rgodf;

  private DIOBJECTDATAFORMAT[] objectDataFormats;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("dwSize"),
          JAVA_INT.withName("dwObjSize"),
          JAVA_INT.withName("dwFlags"),
          JAVA_INT.withName("dwDataSize"),
          JAVA_INT.withName("dwNumObjs"),
          MemoryLayout.paddingLayout(32),
          ADDRESS.withName("rgodf")
  ).withName("DIDATAFORMAT");

  static final VarHandle VH_dwSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSize"));
  static final VarHandle VH_dwObjSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwObjSize"));
  static final VarHandle VH_dwFlags = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwFlags"));
  static final VarHandle VH_dwDataSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwDataSize"));
  static final VarHandle VH_dwNumObjs = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwNumObjs"));
  static final VarHandle VH_rgodf = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("rgodf"));

  public static DIDATAFORMAT read(MemorySegment segment) {
    var data = new DIDATAFORMAT();
    data.dwSize = (int) VH_dwSize.get(segment);
    data.dwObjSize = (int) VH_dwObjSize.get(segment);
    data.dwFlags = (int) VH_dwFlags.get(segment);
    data.dwDataSize = (int) VH_dwDataSize.get(segment);
    data.dwNumObjs = (int) VH_dwNumObjs.get(segment);
    data.rgodf = (MemorySegment) VH_rgodf.get(segment);

    var objetDataFormatPointerSegment = MemorySegment.ofAddress(data.rgodf.address(), DIOBJECTDATAFORMAT.$LAYOUT.byteSize() * data.dwNumObjs, segment.scope());

    data.objectDataFormats = new DIOBJECTDATAFORMAT[data.dwNumObjs];
    for (int i = 0; i < data.dwNumObjs; i++) {
      data.objectDataFormats[i] = DIOBJECTDATAFORMAT.read(objetDataFormatPointerSegment.asSlice(i * DIOBJECTDATAFORMAT.$LAYOUT.byteSize()));
    }

    return data;
  }

  public void write(MemorySegment segment, Arena memoryArena) {
    VH_dwSize.set(segment, dwSize);
    VH_dwObjSize.set(segment, dwObjSize);
    VH_dwFlags.set(segment, dwFlags);
    VH_dwDataSize.set(segment, dwDataSize);
    VH_dwNumObjs.set(segment, dwNumObjs);

    var objectDataFormatSegment = memoryArena.allocate(MemoryLayout.sequenceLayout(dwNumObjs, DIOBJECTDATAFORMAT.$LAYOUT));
    for (int i = 0; i < dwNumObjs; i++) {
      var objectFormat = this.objectDataFormats[i];
      objectFormat.write(objectDataFormatSegment.asSlice(i * DIOBJECTDATAFORMAT.$LAYOUT.byteSize()));
    }

    rgodf = objectDataFormatSegment;
    VH_rgodf.set(segment, rgodf);
  }

  public DIOBJECTDATAFORMAT[] getObjectDataFormats() {
    return this.objectDataFormats;
  }

  public void setObjectDataFormats(DIOBJECTDATAFORMAT[] objectDataFormats) {
    this.objectDataFormats = objectDataFormats;
  }
}
