package com.litiengine.input.natives;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

public class DIDEVICEINSTANCE {
  static final int MAX_PATH = 260;

  public int dwSize = (int) $LAYOUT.byteSize();

  public GUID guidInstance;

  public GUID guidProduct;

  public int dwDevType;

  public char[] tszInstanceName = new char[MAX_PATH];

  public char[] tszProductName = new char[MAX_PATH];

  public GUID guidFFDriver;

  public short wUsagePage;

  public short wUsage;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_INT.withName("dwSize"),
          GUID.$LAYOUT.withName("guidInstance"),
          GUID.$LAYOUT.withName("guidProduct"),
          ValueLayout.JAVA_INT.withName("dwDevType"),
          MemoryLayout.sequenceLayout(MAX_PATH, ValueLayout.JAVA_CHAR).withName("tszInstanceName"),
          MemoryLayout.sequenceLayout(MAX_PATH, ValueLayout.JAVA_CHAR).withName("tszProductName"),
          GUID.$LAYOUT.withName("guidFFDriver"),
          ValueLayout.JAVA_SHORT.withName("wUsagePage"),
          ValueLayout.JAVA_SHORT.withName("wUsage")
  ).withName("DIDEVICEINSTANCE");

  static final VarHandle VH_dwSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSize"));
  static final VarHandle VH_dwDevType = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwDevType"));
  static final VarHandle VH_tszInstanceName = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("tszInstanceName"), MemoryLayout.PathElement.sequenceElement());
  static final VarHandle VH_tszProductName = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("tszProductName"), MemoryLayout.PathElement.sequenceElement());
  static final VarHandle VH_wUsagePage = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wUsagePage"));
  static final VarHandle VH_wUsage = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wUsage"));

  public static DIDEVICEINSTANCE read(MemorySegment segment) {
    var data = new DIDEVICEINSTANCE();
    data.dwSize = (int) VH_dwSize.get(segment);
    // ensure the offset of the cbSize integer before reading the guid
    data.guidInstance = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));
    data.guidProduct = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize() + GUID.$LAYOUT.byteSize()));
    data.dwDevType = (int) VH_dwDevType.get(segment);

    char[] tszInstanceName = new char[MAX_PATH];
    for (int i = 0; i < MAX_PATH; i++) {
      tszInstanceName[i] = (char) VH_tszInstanceName.get(segment, i);
    }

    data.tszInstanceName = tszInstanceName;

    char[] tszProductName = new char[MAX_PATH];
    for (int i = 0; i < MAX_PATH; i++) {
      tszProductName[i] = (char) VH_tszProductName.get(segment, i);
    }

    data.tszProductName = tszProductName;
    data.guidFFDriver = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize() + GUID.$LAYOUT.byteSize() + GUID.$LAYOUT.byteSize() + ValueLayout.JAVA_INT.byteSize() + MAX_PATH + MAX_PATH));
    data.wUsagePage = (short) VH_wUsagePage.get(segment);
    data.wUsage = (short) VH_wUsage.get(segment);
    return data;
  }

  public void write(MemorySegment segment) {
    VH_dwSize.set(segment, dwSize);
    if (guidInstance != null) {
      guidInstance.write(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));
    }

    if (guidProduct != null) {
      guidProduct.write(segment.asSlice(ValueLayout.JAVA_INT.byteSize() + GUID.$LAYOUT.byteSize()));
    }

    VH_dwDevType.set(segment, dwDevType);

    for (int i = 0; i < tszInstanceName.length; i++) {
      VH_tszInstanceName.set(segment, i, tszInstanceName[i]);
    }

    for (int i = 0; i < tszProductName.length; i++) {
      VH_tszProductName.set(segment, i, tszProductName[i]);
    }

    if (guidFFDriver != null) {
      guidFFDriver.write(segment.asSlice(ValueLayout.JAVA_INT.byteSize() + GUID.$LAYOUT.byteSize() + GUID.$LAYOUT.byteSize() + ValueLayout.JAVA_INT.byteSize() + MAX_PATH + MAX_PATH));
    }

    VH_wUsagePage.set(segment, wUsagePage);
    VH_wUsage.set(segment, wUsage);
  }
}
