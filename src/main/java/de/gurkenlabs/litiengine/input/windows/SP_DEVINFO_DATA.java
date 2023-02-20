package de.gurkenlabs.litiengine.input.windows;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * An SP_DEVINFO_DATA structure defines a device instance that is a member of a device information set.
 */
final class SP_DEVINFO_DATA {
  /**
   * The size, in bytes, of the SP_DEVINFO_DATA structure. For more information, see the following Remarks section.
   * <br><br>
   * <p>
   * SetupDiXxx functions that take an SP_DEVINFO_DATA structure as a parameter verify that the cbSize member of the
   * supplied structure is equal to the size, in bytes, of the structure. If the cbSize member is not set correctly
   * for an input parameter, the function will fail and set an error code of ERROR_INVALID_PARAMETER. If the cbSize
   * member is not set correctly for an output parameter, the function will fail and set an error code of
   * ERROR_INVALID_USER_BUFF
   * </p>
   * <br>
   * <p>
   * On 32bit platforms, all SetupApi structures are 1-Byte packed. On 64bit platforms the SetupApi structures are
   * 8-byte packed. IE for 32 bit SP_DEVINFO_DATA.cbSize=28, for 64Bit SP_DEVINFO_DATA.cbSize=(28+4)=32.
   * </p>
   */
  public int cbSize = 32; // 32 will only support 64Bit systems, but that's okay

  /**
   * The GUID of the device's setup class.
   * <p>see <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/install/system-defined-device-setup-classes-available-to-vendors">...</a></p>
   * <b>Some common values:</b>
   * <ul>
   *   <li>Human Interface Devices (HID) - {745a17a0-74d3-11d0-b6fe-00a0c90f57da}</li>
   *   <li>Keyboard - {4d36e96b-e325-11ce-bfc1-08002be10318}</li>
   *   <li>Mouse - {4d36e96f-e325-11ce-bfc1-08002be10318}</li>
   * </ul>
   */
  public GUID ClassGuid;

  /**
   * An opaque handle to the device instance (also known as a handle to the devnode).
   * <br><br>
   * <p>Some functions, such as SetupDiXxx functions, take the whole SP_DEVINFO_DATA structure as input to identify a
   * device in a device information set. Other functions, such as CM_Xxx functions like CM_Get_DevNode_Status,
   * take this DevInst handle as input.</p>
   */
  public int DevInst;

  /**
   * Reserved. For internal use only.
   */
  public long Reserved;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_INT.withName("cbSize"),
          GUID.$LAYOUT.withName("ClassGuid"),
          ValueLayout.JAVA_INT.withName("DevInst"),
          ValueLayout.JAVA_LONG.withName("Reserved")
  ).withName("SP_DEVINFO_DATA");

  static final VarHandle VH_cbSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("cbSize"));
  static final VarHandle VH_DevInst = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("DevInst"));
  static final VarHandle VH_Reserved = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Reserved"));

  public static SP_DEVINFO_DATA read(MemorySegment segment) {
    var data = new SP_DEVINFO_DATA();
    data.cbSize = (int) VH_cbSize.get(segment);

    // ensure the offset of the cbSize integer before reading the guid
    data.ClassGuid = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));
    data.DevInst = (int) VH_DevInst.get(segment);
    data.Reserved = (long) VH_Reserved.get(segment);
    return data;
  }

  public void write(MemorySegment segment) {
    VH_cbSize.set(segment, cbSize);
    if (ClassGuid != null) {
      ClassGuid.write(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));
    }
    VH_DevInst.set(segment, DevInst);
    VH_Reserved.set(segment, Reserved);
  }
}
