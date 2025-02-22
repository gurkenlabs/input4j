package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

class IOHIDDeviceInterface {
  private MemorySegment thisPointer;
  short version;
  short revision;

  private MethodHandle release;
  private MethodHandle open;
  private MethodHandle close;
  private MethodHandle getElementValue;
  private MethodHandle setElementValue;
  private MethodHandle allocQueue;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_SHORT.withName("version"),
          JAVA_SHORT.withName("revision"),
          ADDRESS.withName("QueryInterface"),
          ADDRESS.withName("AddRef"),
          ADDRESS.withName("Release"),
          ADDRESS.withName("createAsyncEventSource"),
          ADDRESS.withName("getAsyncEventSource"),
          ADDRESS.withName("createAsyncPort"),
          ADDRESS.withName("getAsyncPort"),
          ADDRESS.withName("open"),
          ADDRESS.withName("close"),
          ADDRESS.withName("setReport"),
          ADDRESS.withName("getReport"),
          ADDRESS.withName("setRemovalCallback"),
          ADDRESS.withName("getElementValue"),
          ADDRESS.withName("setElementValue"),
          ADDRESS.withName("queryElementValue"),
          ADDRESS.withName("startAllQueues"),
          ADDRESS.withName("stopAllQueues"),
          ADDRESS.withName("allocQueue"),
          ADDRESS.withName("allocOutputTransaction")
  ).withName("IOHIDDeviceInterface");

  private static final FunctionDescriptor releaseDescriptor = FunctionDescriptor.ofVoid(ADDRESS);
  private static final FunctionDescriptor openDescriptor = FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT);
  private static final FunctionDescriptor closeDescriptor = FunctionDescriptor.ofVoid(ADDRESS);
  private static final FunctionDescriptor getElementValueDescriptor = FunctionDescriptor.of(ADDRESS, JAVA_INT, ADDRESS);
  private static final FunctionDescriptor setElementValueDescriptor = FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS);
  private static final FunctionDescriptor allocQueueDescriptor = FunctionDescriptor.of(ADDRESS);

  static final VarHandle VH_version = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("version"));
  static final VarHandle VH_revision = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("revision"));
  static final VarHandle VH_release = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Release"));
  static final VarHandle VH_open = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("open"));
  static final VarHandle VH_close = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("close"));
  static final VarHandle VH_getElementValue = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("getElementValue"));
  static final VarHandle VH_setElementValue = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("setElementValue"));
  static final VarHandle VH_allocQueue = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("allocQueue"));

  public static IOHIDDeviceInterface read(MemorySegment segment) {
    var deviceInterface = new IOHIDDeviceInterface();
    deviceInterface.thisPointer = segment;

    deviceInterface.version = (short) VH_version.get(segment, 0);
    deviceInterface.revision = (short) VH_revision.get(segment, 0);
    deviceInterface.release = downcallHandle((MemorySegment) VH_release.get(segment, 0), releaseDescriptor);
    deviceInterface.open = downcallHandle((MemorySegment) VH_open.get(segment, 0), openDescriptor);
    deviceInterface.close = downcallHandle((MemorySegment) VH_close.get(segment, 0), closeDescriptor);
    deviceInterface.getElementValue = downcallHandle((MemorySegment) VH_getElementValue.get(segment, 0), getElementValueDescriptor);
    deviceInterface.setElementValue = downcallHandle((MemorySegment) VH_setElementValue.get(segment, 0), setElementValueDescriptor);
    deviceInterface.allocQueue = downcallHandle((MemorySegment) VH_allocQueue.get(segment, 0), allocQueueDescriptor);

    return deviceInterface;
  }

  public void release() throws Throwable {
    release.invokeExact(thisPointer);
  }

  public void open() throws Throwable {
    open.invokeExact(thisPointer, 0);
  }

  public void close() throws Throwable {
    close.invokeExact(thisPointer);
  }

  public MemorySegment getElementValue(int elementCookie, MemorySegment valueEvent) throws Throwable {
    return (MemorySegment) getElementValue.invokeExact(thisPointer, elementCookie, valueEvent);
  }

  public void setElementValue(MemorySegment element, MemorySegment event) throws Throwable {
    setElementValue.invokeExact(thisPointer, element, event);
  }

  public IOHIDQueueInterface allocQueue() throws Throwable {
    return IOHIDQueueInterface.read((MemorySegment) allocQueue.invokeExact(thisPointer));
  }
}