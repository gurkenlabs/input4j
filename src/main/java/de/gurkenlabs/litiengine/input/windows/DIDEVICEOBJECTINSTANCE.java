package de.gurkenlabs.litiengine.input.windows;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

final class DIDEVICEOBJECTINSTANCE {
  final static GUID GUID_XAxis = new GUID(0xA36D02E0, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_YAxis = new GUID(0xA36D02E1, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_ZAxis = new GUID(0xA36D02E2, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_RxAxis = new GUID(0xA36D02F4, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_RyAxis = new GUID(0xA36D02F5, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_RzAxis = new GUID(0xA36D02E3, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_Slider = new GUID(0xA36D02E4, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_Button = new GUID(0xA36D02F0, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_Key = new GUID(0x55728220, (short) 0xD33C, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_POV = new GUID(0xA36D02F2, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);
  final static GUID GUID_Unknown = new GUID(0xA36D02F3, (short) 0xC9F3, (short) 0x11CF, (byte) 0xBF, (byte) 0xC7, (byte) 0x44, (byte) 0x45, (byte) 0x53, (byte) 0x54, (byte) 0x00, (byte) 0x00);

  final static int DIDOI_FFACTUATOR		= 0x00000001;
  final static int DIDOI_FFEFFECTTRIGGER   = 0x00000002;
  final static int DIDOI_POLLED			= 0x00008000;
  final static int DIDOI_ASPECTPOSITION	= 0x00000100;
  final static int DIDOI_ASPECTVELOCITY	= 0x00000200;
  final static int DIDOI_ASPECTACCEL	   = 0x00000300;
  final static int DIDOI_ASPECTFORCE	   = 0x00000400;
  final static int DIDOI_ASPECTMASK		= 0x00000F00;
  final static int DIDOI_GUIDISUSAGE	   = 0x00010000;

  public int dwSize = (int) $LAYOUT.byteSize();

  /**
   * Unique identifier that indicates the object type.
   */
  public GUID guidType;

  public int dwOfs;

  public int dwType;

  public int dwFlags;

  public char[] tszName = new char[DIDEVICEINSTANCE.MAX_PATH];

  public int dwFFMaxForce;

  public int dwFFForceResolution;

  public short wCollectionNumber;

  public short wDesignatorIndex;

  public short wUsagePage;

  public short wUsage;

  public int dwDimension;

  public short wExponent;

  public short wReportId;

  public int getInstance(){
    return dwType >> 8;
  }

  public int getType (){
    return dwType & 0xFF;
  }

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("dwSize"),
          GUID.$LAYOUT.withName("guidType"),
          JAVA_INT.withName("dwOfs"),
          JAVA_INT.withName("dwType"),
          JAVA_INT.withName("dwFlags"),
          MemoryLayout.sequenceLayout(DIDEVICEINSTANCE.MAX_PATH, ValueLayout.JAVA_CHAR).withName("tszName"),
          JAVA_INT.withName("dwFFMaxForce"),
          JAVA_INT.withName("dwFFForceResolution"),
          JAVA_SHORT.withName("wCollectionNumber"),
          JAVA_SHORT.withName("wDesignatorIndex"),
          JAVA_SHORT.withName("wUsagePage"),
          JAVA_SHORT.withName("wUsage"),
          JAVA_INT.withName("dwDimension"),
          JAVA_SHORT.withName("wExponent"),
          JAVA_SHORT.withName("wReportId")
  ).withName("DIDEVICEOBJECTINSTANCEW");

  static final VarHandle VH_dwSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSize"));
  static final VarHandle VH_dwOfs = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwOfs"));
  static final VarHandle VH_dwType = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwType"));
  static final VarHandle VH_dwFlags = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwFlags"));
  static final VarHandle VH_tszName = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("tszName"), MemoryLayout.PathElement.sequenceElement());
  static final VarHandle VH_dwFFMaxForce = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwFFMaxForce"));
  static final VarHandle VH_dwFFForceResolution = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwFFForceResolution"));
  static final VarHandle VH_wCollectionNumber = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wCollectionNumber"));
  static final VarHandle VH_wDesignatorIndex = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wDesignatorIndex"));
  static final VarHandle VH_wUsagePage = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wUsagePage"));
  static final VarHandle VH_wUsage = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wUsage"));
  static final VarHandle VH_dwDimension = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwDimension"));
  static final VarHandle VH_wExponent = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wExponent"));
  static final VarHandle VH_wReportId = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wReportId"));

  public static DIDEVICEOBJECTINSTANCE read(MemorySegment segment) {
    var data = new DIDEVICEOBJECTINSTANCE();
    data.dwSize = (int) VH_dwSize.get(segment);
    // ensure the offset of the dwSize integer before reading the guid
    data.guidType = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));

    data.dwOfs = (int) VH_dwOfs.get(segment);
    data.dwType = (int) VH_dwType.get(segment);
    data.dwFlags = (int) VH_dwFlags.get(segment);

    char[] tszName = new char[DIDEVICEINSTANCE.MAX_PATH];
    for (int i = 0; i < DIDEVICEINSTANCE.MAX_PATH; i++) {
      tszName[i] = (char) VH_tszName.get(segment, i);
    }

    data.tszName = tszName;
    data.dwFFMaxForce = (int) VH_dwFFMaxForce.get(segment);
    data.dwFFForceResolution = (int) VH_dwFFForceResolution.get(segment);
    data.wCollectionNumber = (short) VH_wCollectionNumber.get(segment);
    data.wDesignatorIndex = (short) VH_wDesignatorIndex.get(segment);
    data.wUsagePage = (short) VH_wUsagePage.get(segment);
    data.wUsage = (short) VH_wUsage.get(segment);
    data.dwDimension = (int) VH_dwDimension.get(segment);
    data.wExponent = (short) VH_wExponent.get(segment);
    data.wReportId = (short) VH_wReportId.get(segment);
    return data;
  }

}
