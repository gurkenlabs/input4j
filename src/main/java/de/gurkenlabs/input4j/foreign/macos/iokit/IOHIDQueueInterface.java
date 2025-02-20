package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

class IOHIDQueueInterface {
  private MemorySegment thisPointer;
  public short version;
  public short revision;
  private MethodHandle release;
  private MethodHandle addElement;
  private MethodHandle create;
  private MethodHandle dispose;
  private MethodHandle getNextEvent;
  private MethodHandle removeElement;
  private MethodHandle start;
  private MethodHandle stop;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_SHORT.withName("version"),
          JAVA_SHORT.withName("revision"),
          ADDRESS.withName("_reserved"),
          ADDRESS.withName("QueryInterface"),
          ADDRESS.withName("AddRef"),
          ADDRESS.withName("Release"),
          // Queue-specific methods follow
          ADDRESS.withName("createAsyncEventSource"),
          ADDRESS.withName("createAsyncPort"),
          ADDRESS.withName("getAsyncEventSource"),
          ADDRESS.withName("getAsyncPort"),
          ADDRESS.withName("create"),
          ADDRESS.withName("dispose"),
          ADDRESS.withName("addElement"),
          ADDRESS.withName("removeElement"),
          ADDRESS.withName("hasElement"),
          ADDRESS.withName("setEventCallout"),
          ADDRESS.withName("getEventCallout"),
          ADDRESS.withName("getNextEvent"),
          ADDRESS.withName("start"),
          ADDRESS.withName("stop")
  ).withName("IOHIDQueueInterface");

  private static final FunctionDescriptor releaseDescriptor = FunctionDescriptor.ofVoid(ADDRESS);
  private static final FunctionDescriptor addElementDescriptor = FunctionDescriptor.ofVoid(ADDRESS, ADDRESS);
  private static final FunctionDescriptor createDescriptor = FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT);
  private static final FunctionDescriptor disposeDescriptor = FunctionDescriptor.ofVoid(ADDRESS);
  private static final FunctionDescriptor getNextEventDescriptor = FunctionDescriptor.of(ADDRESS, ADDRESS);
  private static final FunctionDescriptor removeElementDescriptor = FunctionDescriptor.ofVoid(ADDRESS, ADDRESS);
  private static final FunctionDescriptor startDescriptor = FunctionDescriptor.ofVoid(ADDRESS);
  private static final FunctionDescriptor stopDescriptor = FunctionDescriptor.ofVoid(ADDRESS);

  static final VarHandle VH_version = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("version"));
  static final VarHandle VH_revision = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("revision"));
  static final VarHandle VH_release = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Release"));
  static final VarHandle VH_addElement = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("addElement"));
  static final VarHandle VH_create = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("create"));
  static final VarHandle VH_dispose = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dispose"));
  static final VarHandle VH_getNextEvent = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("getNextEvent"));
  static final VarHandle VH_removeElement = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("removeElement"));
  static final VarHandle VH_start = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("start"));
  static final VarHandle VH_stop = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("stop"));

  public static IOHIDQueueInterface read(MemorySegment segment) {
    var queueInterface = new IOHIDQueueInterface();
    queueInterface.thisPointer = segment;

    queueInterface.version = (short) VH_version.get(segment, 0);
    queueInterface.revision = (short) VH_revision.get(segment, 0);
    queueInterface.release = downcallHandle((MemorySegment) VH_release.get(segment, 0), releaseDescriptor);
    queueInterface.addElement = downcallHandle((MemorySegment) VH_addElement.get(segment, 0), addElementDescriptor);
    queueInterface.create = downcallHandle((MemorySegment) VH_create.get(segment, 0), createDescriptor);
    queueInterface.dispose = downcallHandle((MemorySegment) VH_dispose.get(segment, 0), disposeDescriptor);
    queueInterface.getNextEvent = downcallHandle((MemorySegment) VH_getNextEvent.get(segment, 0), getNextEventDescriptor);
    queueInterface.removeElement = downcallHandle((MemorySegment) VH_removeElement.get(segment, 0), removeElementDescriptor);
    queueInterface.start = downcallHandle((MemorySegment) VH_start.get(segment, 0), startDescriptor);
    queueInterface.stop = downcallHandle((MemorySegment) VH_stop.get(segment, 0), stopDescriptor);

    return queueInterface;
  }

  public void release() throws Throwable {
    release.invokeExact(thisPointer);
  }

  public void addElement(MemorySegment element) throws Throwable {
    addElement.invokeExact(thisPointer, element);
  }

  public void create(int queue_depth) throws Throwable {
    create.invokeExact(thisPointer);
  }

  public void dispose() throws Throwable {
    dispose.invokeExact(thisPointer);
  }

  public MemorySegment getNextEvent() throws Throwable {
    return (MemorySegment) getNextEvent.invokeExact(thisPointer);
  }

  public void removeElement(MemorySegment element) throws Throwable {
    removeElement.invokeExact(thisPointer, element);
  }

  public void start() throws Throwable {
    start.invokeExact(thisPointer);
  }

  public void stop() throws Throwable {
    stop.invokeExact(thisPointer);
  }
}