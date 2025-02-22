package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public class IOHIDValueRef {
  // Hypothetical layout of the __IOHIDValue struct:
  // - A CFIndex (typically a signed long) for the value.
  // - A 64-bit timestamp.
  // - A pointer to the associated IOHIDElement.
  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_LONG.withName("value"),
          JAVA_LONG.withName("timestamp"),
          ADDRESS.withName("element")

  ).withName("IOHIDValueRef");
}
