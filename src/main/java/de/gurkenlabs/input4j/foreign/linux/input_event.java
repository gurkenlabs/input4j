package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

/*
 * The event structure itself
 */
class input_event {
  public timeval time;
  public int type;
  public int code;
  public int value;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          timeval.$LAYOUT.withName("time"),
          JAVA_INT.withName("type"),
          JAVA_INT.withName("code"),
          JAVA_INT.withName("value")
  ).withName("input_event");

  static final VarHandle VH_type = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("type"));
  static final VarHandle VH_code = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("code"));
  static final VarHandle VH_value = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("value"));

  public static input_event read(MemorySegment segment) {
    var inputEvent = new input_event();
    inputEvent.time = timeval.read(segment);
    inputEvent.type = (int) VH_type.get(segment);
    inputEvent.code = (int) VH_code.get(segment);
    inputEvent.value = (int) VH_value.get(segment);

    return inputEvent;
  }

  public void write(MemorySegment segment) {
    time.write(segment);
    VH_type.set(segment, type);
    VH_code.set(segment, code);
    VH_value.set(segment, value);
  }
}
