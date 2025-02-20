package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

class IOHIDEventStruct {
  public long timeStamp;      // AbsoluteTime maps to uint64_t
  public int type;           // IOHIDElementType maps to uint32_t
  public int elementCookie;  // IOHIDElementCookie maps to uint32_t
  public int value;         // SInt32 maps to int32_t
  public int flags;         // IOHIDEventFlags

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_LONG.withName("timeStamp"),
          JAVA_INT.withName("type"),
          JAVA_INT.withName("elementCookie"),
          JAVA_INT.withName("value"),
          JAVA_INT.withName("flags")
  ).withName("IOHIDEventStruct");

  static final VarHandle VH_timeStamp = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("timeStamp"));
  static final VarHandle VH_type = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("type"));
  static final VarHandle VH_elementCookie = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("elementCookie"));
  static final VarHandle VH_value = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("value"));
  static final VarHandle VH_flags = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("flags"));

  public static IOHIDEventStruct read(MemorySegment segment) {
    var event = new IOHIDEventStruct();
    event.timeStamp = (long) VH_timeStamp.get(segment, 0);
    event.type = (int) VH_type.get(segment, 0);
    event.elementCookie = (int) VH_elementCookie.get(segment, 0);
    event.value = (int) VH_value.get(segment, 0);
    event.flags = (int) VH_flags.get(segment, 0);
    return event;
  }

  public void write(MemorySegment segment) {
    VH_timeStamp.set(segment, 0, timeStamp);
    VH_type.set(segment, 0, type);
    VH_elementCookie.set(segment, 0, elementCookie);
    VH_value.set(segment, 0, value);
    VH_flags.set(segment, 0, flags);
  }
}
