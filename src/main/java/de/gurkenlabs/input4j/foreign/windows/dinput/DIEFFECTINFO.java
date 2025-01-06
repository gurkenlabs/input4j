package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

final class DIEFFECTINFO {
  static final int MAX_STRING_LENGTH = 260;

  // all effect parameter flags that can be used in dwStaticParams and dwDynamicParams
  public final static int DIEP_DURATION			   = 0x00000001;
  public final static int DIEP_SAMPLEPERIOD		   = 0x00000002;
  public final static int DIEP_GAIN				   = 0x00000004;
  public final static int DIEP_TRIGGERBUTTON		  = 0x00000008;
  public final static int DIEP_TRIGGERREPEATINTERVAL  = 0x00000010;
  public final static int DIEP_AXES				   = 0x00000020;
  public final static int DIEP_DIRECTION			  = 0x00000040;
  public final static int DIEP_ENVELOPE			   = 0x00000080;
  public final static int DIEP_TYPESPECIFICPARAMS	 = 0x00000100;
  public final static int DIEP_STARTDELAY			 = 0x00000200;
  public final static int DIEP_ALLPARAMS_DX5		  = 0x000001FF;
  public final static int DIEP_ALLPARAMS			  = 0x000003FF;
  public final static int DIEP_START				  = 0x20000000;
  public final static int DIEP_NORESTART			  = 0x40000000;
  public final static int DIEP_NODOWNLOAD			 = 0x80000000;

  public int dwSize;
  public GUID guid;
  public int dwEffType;
  public int dwStaticParams;
  public int dwDynamicParams;
  public char[] tszName = new char[MAX_STRING_LENGTH];

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("dwSize"),
          GUID.$LAYOUT.withName("guid"),
          JAVA_INT.withName("dwEffType"),
          JAVA_INT.withName("dwStaticParams"),
          JAVA_INT.withName("dwDynamicParams"),
          MemoryLayout.sequenceLayout(MAX_STRING_LENGTH, ValueLayout.JAVA_CHAR).withName("tszName")
  ).withName("DIEFFECTINFO");

  static final VarHandle VH_dwSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSize"));
  static final VarHandle VH_dwEffType = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwEffType"));
  static final VarHandle VH_dwStaticParams = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwStaticParams"));
  static final VarHandle VH_dwDynamicParams = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwDynamicParams"));
  static final VarHandle VH_tszName = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("tszName"), MemoryLayout.PathElement.sequenceElement());

  public static DIEFFECTINFO read(MemorySegment segment) {
    var data = new DIEFFECTINFO();
    data.dwSize = (int) VH_dwSize.get(segment, 0);
    data.guid = GUID.read(segment.asSlice($LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("guid"))));
    data.dwEffType = (int) VH_dwEffType.get(segment, 0);
    data.dwStaticParams = (int) VH_dwStaticParams.get(segment, 0);
    data.dwDynamicParams = (int) VH_dwDynamicParams.get(segment, 0);

    char[] tszInstanceName = new char[MAX_STRING_LENGTH];
    for (int i = 0; i < MAX_STRING_LENGTH; i++) {
      tszInstanceName[i] = (char) VH_tszName.get(segment,0, i);
    }

    data.tszName = tszInstanceName;
    return data;
  }
}
