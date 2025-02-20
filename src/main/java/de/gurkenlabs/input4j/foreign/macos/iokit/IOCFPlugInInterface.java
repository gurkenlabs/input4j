package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

class IOCFPlugInInterface {
  private MemorySegment thisPointer;
  public short version;
  public short revision;
  private MethodHandle queryInterface;
  private MethodHandle release;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_SHORT.withName("version"),
          JAVA_SHORT.withName("revision"),
          ADDRESS.withName("_reserved"),
          ADDRESS.withName("QueryInterface"),
          ADDRESS.withName("AddRef"),
          ADDRESS.withName("Release"),
          ADDRESS.withName("Probe"),
          ADDRESS.withName("Start"),
          ADDRESS.withName("Stop")
  ).withName("IOCFPlugInInterface");

  private static final FunctionDescriptor queryInterfaceDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS);
  private static final FunctionDescriptor releaseDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);

  static final VarHandle VH_version = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("version"));
  static final VarHandle VH_revision = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("revision"));
  static final VarHandle VH_queryInterface = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("QueryInterface"));
  static final VarHandle VH_release = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Release"));

  public static IOCFPlugInInterface read(MemorySegment segment) {
    var pluginInterface = new IOCFPlugInInterface();
    pluginInterface.thisPointer = segment;
    pluginInterface.version = (short) VH_version.get(segment, 0);
    pluginInterface.revision = (short) VH_revision.get(segment, 0);

    var queryInterfacePointer = (MemorySegment) VH_queryInterface.get(segment, 0);
    pluginInterface.queryInterface = downcallHandle(queryInterfacePointer, queryInterfaceDescriptor);

    var releasePointer = (MemorySegment) VH_release.get(segment, 0);
    pluginInterface.release = downcallHandle(releasePointer, releaseDescriptor);

    return pluginInterface;
  }

  public int QueryInterface(MemorySegment iid, MemorySegment ppv) throws Throwable {
    return (int) queryInterface.invokeExact(thisPointer, iid, ppv);
  }

  public int release() throws Throwable {
    return (int) release.invokeExact(thisPointer);
  }
}