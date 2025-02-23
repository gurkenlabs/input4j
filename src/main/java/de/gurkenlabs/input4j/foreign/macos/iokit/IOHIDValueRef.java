package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
import java.time.ZonedDateTime;

import static java.lang.foreign.ValueLayout.*;

public class IOHIDValueRef {
  long timestamp;
  long integerValue;
  double scaledValue;
  long element;

  // Hypothetical layout of the __IOHIDValue struct:
  // - A 64-bit timestamp.
  // - A CFIndex (typically a signed long) for the integer value.
  // - A double for the scaled value.
  // - A pointer to the associated IOHIDElement.
  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_LONG.withName("timestamp"),
          JAVA_LONG.withName("integerValue"),
          JAVA_DOUBLE.withName("scaledValue"),
          ADDRESS.withName("element")
  ).withName("IOHIDValueRef");

  static final VarHandle VH_timestamp = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("timestamp"));
  static final VarHandle VH_integerValue = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("integerValue"));
  static final VarHandle VH_scaledValue = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("scaledValue"));
  static final VarHandle VH_element = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("element"));

  public static IOHIDValueRef read(MemorySegment segment) {
    var valueRef = new IOHIDValueRef();
    valueRef.timestamp = (long) VH_timestamp.get(segment, 0);
    valueRef.integerValue = (long) VH_integerValue.get(segment, 0);
    valueRef.scaledValue = (double) VH_scaledValue.get(segment, 0);
    valueRef.element = (long) VH_element.get(segment, 0);
    return valueRef;
  }

  public ZonedDateTime getTimestamp() {
    return ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault());
  }

}
