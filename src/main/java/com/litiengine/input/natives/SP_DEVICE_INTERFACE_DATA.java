package com.litiengine.input.natives;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

final class SP_DEVICE_INTERFACE_DATA {
  /**
   * The size, in bytes, of the SP_DEVICE_INTERFACE_DATA structure. For more information, see the Remarks section.
   * <br><br>
   * <p>
   * A SetupAPI function that takes an instance of the SP_DEVICE_INTERFACE_DATA structure as a parameter verifies
   * whether the cbSize member of the supplied structure is equal to the size, in bytes, of the structure. If the
   * cbSize member is not set correctly, the function will fail and set an error code of ERROR_INVALID_USER_BUFFER.
   * </p>
   */
  public int cbSize = (int) ($LAYOUT.byteSize());

  public GUID InterfaceClassGuid;

  public int Flags;

  public long Reserved;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_INT.withName("cbSize"),
          GUID.$LAYOUT.withName("InterfaceClassGuid"),
          ValueLayout.JAVA_INT.withName("Flags"),
          ValueLayout.JAVA_LONG.withName("Reserved")
  ).withName("SP_DEVICE_INTERFACE_DATA");

  static final VarHandle VH_cbSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("cbSize"));
  static final VarHandle VH_Flags = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Flags"));
  static final VarHandle VH_Reserved = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Reserved"));

  public static SP_DEVICE_INTERFACE_DATA read(MemorySegment segment) {
    var data = new SP_DEVICE_INTERFACE_DATA();
    data.cbSize = (int) VH_cbSize.get(segment);

    // ensure the offset of the cbSize integer before reading the guid
    data.InterfaceClassGuid = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));
    data.Flags = (int) VH_Flags.get(segment);
    data.Reserved = (long) VH_Reserved.get(segment);
    return data;
  }

  public void write(MemorySegment segment) {
    VH_cbSize.set(segment, cbSize);
    if (InterfaceClassGuid != null) {
      InterfaceClassGuid.write(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));
    }
    VH_Flags.set(segment, Flags);
    VH_Reserved.set(segment, Reserved);
  }
}
