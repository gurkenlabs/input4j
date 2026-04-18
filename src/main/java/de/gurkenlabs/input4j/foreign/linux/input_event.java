package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/*
 * The event structure itself.
 *
 * <p>
 * Corresponds to {@code struct input_event} from {@code linux/input.h}.
 * The {@code time} field contains a timestamp for the event. When writing events
 * to the device (e.g., for force feedback), the kernel ignores zero timestamps
 * or replaces them with the current time, so initializing to zero is safe
 * and the preferred approach for user-space generated events.
 *
 * @see <a href="https://www.kernel.org/doc/html/latest/input/uinput.html">uinput module</a>
 */
class input_event {
  public timeval time = new timeval();
  public short type;
  public short code;
  public int value;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          timeval.$LAYOUT.withName("time"),
          JAVA_SHORT.withName("type"),
          JAVA_SHORT.withName("code"),
          JAVA_INT.withName("value")
  ).withName("input_event");

  static final VarHandle VH_type = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("type"));
  static final VarHandle VH_code = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("code"));
  static final VarHandle VH_value = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("value"));

  public static input_event read(MemorySegment segment) {
    var inputEvent = new input_event();
    inputEvent.time = timeval.read(segment);
    inputEvent.type = (short) VH_type.get(segment, 0);
    inputEvent.code = (short) VH_code.get(segment, 0);
    inputEvent.value = (int) VH_value.get(segment, 0);

    return inputEvent;
  }

  public void write(MemorySegment segment) {
    time.write(segment);
    VH_type.set(segment, 0, type);
    VH_code.set(segment, 0, code);
    VH_value.set(segment, 0, value);
  }
}
