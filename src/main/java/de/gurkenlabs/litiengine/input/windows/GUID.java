package de.gurkenlabs.litiengine.input.windows;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * A GUID identifies an object such as a COM interfaces, or a COM class object, or a manager entry-point vector (EPV).
 * A GUID is a 128-bit value consisting of one group of 8 hexadecimal digits, followed by three groups of 4 hexadecimal
 * digits each, followed by one group of 12 hexadecimal digits. The following example GUID shows the groupings of
 * hexadecimal digits in a GUID: 6B29FC40-CA47-1067-B31D-00DD010662DA.
 */
final class GUID {
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
  public final int Data1;
  public final short Data2;
  public final short Data3;
  public final byte[] Data4;

  /**
   * @param Data1 Specifies the first 8 hexadecimal digits of the GUID.
   * @param Data2 Specifies the first group of 4 hexadecimal digits.
   * @param Data3 Specifies the second group of 4 hexadecimal digits.
   * @param Data4 Array of 8 bytes. The first 2 bytes contain the third group of 4 hexadecimal digits.
   *              The remaining 6 bytes contain the final 12 hexadecimal digits.
   */
  GUID(int Data1, short Data2, short Data3, byte... Data4) {
    this.Data1 = Data1;
    this.Data2 = Data2;
    this.Data3 = Data3;
    this.Data4 = Data4;
  }

  GUID(int Data1, int Data2, int Data3, int Data4_1, int Data4_2, int Data4_3, int Data4_4, int Data4_5, int Data4_6, int Data4_7, int Data4_8) {
    this.Data1 = Data1;
    this.Data2 = (short) Data2;
    this.Data3 = (short) Data3;
    this.Data4 = new byte[]{(byte) Data4_1, (byte) Data4_2, (byte) Data4_3, (byte) Data4_4, (byte) Data4_5, (byte) Data4_6, (byte) Data4_7, (byte) Data4_8,};
  }

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

  public MemorySegment write(Arena memorySession) {
    var memorySegment = memorySession.allocate($LAYOUT);
    write(memorySegment);
    return memorySegment;
  }

  public void write(MemorySegment segment) {
    VH_Data1.set(segment, Data1);
    VH_Data2.set(segment, Data2);
    VH_Data3.set(segment, Data3);

    for (int i = 0; i < DATA4_LENGTH; i++) {
      VH_Data4.set(segment, i, Data4[i]);
    }
  }


  public UUID toUUID() {
    return UUID.fromString(this.rawString());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (GUID) obj;
    return this.Data1 == that.Data1 &&
            this.Data2 == that.Data2 &&
            this.Data3 == that.Data3 &&
            Arrays.equals(this.Data4, that.Data4);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Data1, Data2, Data3, Arrays.hashCode(Data4));
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
}
