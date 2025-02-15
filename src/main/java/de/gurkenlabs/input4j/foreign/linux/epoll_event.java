package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * Represents the native {@code epoll_event} structure used in Linux's epoll API.
 *
 * <p>The native structure is defined as:
 * <pre>
 * struct epoll_event {
 *     uint32_t     events;  // Bit mask of events
 *     union epoll_data {
 *         void     *ptr;
 *         int       fd;
 *         uint32_t  u32;
 *         uint64_t  u64;
 *     } data;             // User data (union, occupying 8 bytes)
 * };
 * </pre>
 * In this Java implementation, we simplify the {@code data} union by representing it as a
 * 4-byte integer field along with additional padding. This is done to ensure that the overall
 * layout matches the native structure's size and alignment (16 bytes total):
 * <ul>
 *   <li>4 bytes for the {@code events} field.</li>
 *   <li>4 bytes of padding after {@code events}.</li>
 *   <li>4 bytes for the simplified {@code data} field.</li>
 *   <li>4 bytes of padding after {@code data}.</li>
 * </ul>
 *
 * <p>This layout is defined in the {@code $LAYOUT} constant below and ensures compatibility
 * with the native epoll interface when performing memory operations using the Java Foreign Memory API.</p>
 *
 * @see <a href="https://man7.org/linux/man-pages/man7/epoll.7.html">epoll(7) - Linux manual page</a>
 */
class epoll_event {
  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("events"),
          MemoryLayout.paddingLayout(4),
          JAVA_INT.withName("data"),
          MemoryLayout.paddingLayout(4)
  ).withName("epoll_event");

  static final VarHandle VH_events = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("events"));
  static final VarHandle VH_data_fd = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("data"));

  public static epoll_event read(MemorySegment segment) {
    var event = new epoll_event();
    event.events = (int) VH_events.get(segment, 0);
    event.data_fd = (int) VH_data_fd.get(segment, 0);
    return event;
  }

  public void write(MemorySegment segment) {
    VH_events.set(segment, 0, this.events);
    VH_data_fd.set(segment, 0, this.data_fd);
  }

  public int events;
  public int data_fd;
}
