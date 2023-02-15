package de.gurkenlabs.litiengine.input.natives;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.UUID;

/**
 * A GUID identifies an object such as a COM interfaces, or a COM class object, or a manager entry-point vector (EPV).
 * A GUID is a 128-bit value consisting of one group of 8 hexadecimal digits, followed by three groups of 4 hexadecimal
 * digits each, followed by one group of 12 hexadecimal digits. The following example GUID shows the groupings of
 * hexadecimal digits in a GUID: 6B29FC40-CA47-1067-B31D-00DD010662DA.
 *
 * @param Data1 Specifies the first 8 hexadecimal digits of the GUID.
 * @param Data2 Specifies the first group of 4 hexadecimal digits.
 * @param Data3 Specifies the second group of 4 hexadecimal digits.
 * @param Data4 Array of 8 bytes. The first 2 bytes contain the third group of 4 hexadecimal digits.
 *              The remaining 6 bytes contain the final 12 hexadecimal digits.
 */
record GUID(int Data1, short Data2, short Data3, byte... Data4) {
  static int DATA4_LENGTH = 8;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_INT.withName("Data1"),
          ValueLayout.JAVA_SHORT.withName("Data2"),
          ValueLayout.JAVA_SHORT.withName("Data3"),
          MemoryLayout.sequenceLayout(8, ValueLayout.JAVA_BYTE).withName("Data4")
  ).withName("GUID");

  static final VarHandle VH_Data1 = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Data1"));
  static final VarHandle VH_Data2 = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Data2"));
  static final VarHandle VH_Data3 = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Data3"));
  static final VarHandle VH_Data4 = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Data4"), MemoryLayout.PathElement.sequenceElement());

  public static GUID read(MemorySegment segment) {
    var data1 = (int) VH_Data1.get(segment);
    var data2 = (short) VH_Data2.get(segment);
    var data3 = (short) VH_Data3.get(segment);

    byte[] data4 = new byte[DATA4_LENGTH];
    for (int i = 0; i < DATA4_LENGTH; i++) {
      data4[i] = (byte) VH_Data4.get(segment, i);
    }

    return new GUID(data1, data2, data3, data4);
  }

  public void write(MemorySegment segment) {
    VH_Data1.set(segment, Data1());
    VH_Data2.set(segment, Data2());
    VH_Data3.set(segment, Data3());

    for (int i = 0; i < DATA4_LENGTH; i++) {
      VH_Data4.set(segment, i, Data4()[i]);
    }
  }

  @Override
  public String toString() {
    return ("{" + this.rawString() + "}").toUpperCase();
  }

  private String rawString() {
    var sb = new StringBuilder();
    sb.append(String.format("%08X", Data1));
    sb.append("-");
    sb.append(String.format("%04X", Data2));
    sb.append("-");
    sb.append(String.format("%04X", Data3));
    sb.append("-");

    for (int i = 0; i < Data4.length; i++) {
      sb.append(String.format("%02X", Data4[i]));
      if (i == 1) {
        sb.append("-");
      }
    }

    return sb.toString();
  }

  public UUID toUUID() {
    return UUID.fromString(this.rawString());
  }
}
