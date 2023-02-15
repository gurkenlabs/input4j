package com.litiengine.input.natives;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

public class IDirectInput8WVtbl {
  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ADDRESS.withName("QueryInterface"),
          ADDRESS.withName("AddRef"),
          ADDRESS.withName("Release"),
          ADDRESS.withName("CreateDevice"),
          ADDRESS.withName("EnumDevices"),
          ADDRESS.withName("GetDeviceStatus"),
          ADDRESS.withName("RunControlPanel"),
          ADDRESS.withName("Initialize"),
          ADDRESS.withName("FindDevice"),
          ADDRESS.withName("EnumDevicesBySemantics"),
          ADDRESS.withName("ConfigureDevices")
  ).withName("IDirectInput8WVtbl");

  static final FunctionDescriptor EnumDevices$FUNC = FunctionDescriptor.of(JAVA_INT,
          ADDRESS,
          JAVA_INT,
          ADDRESS,
          ADDRESS,
          JAVA_INT
  );

  static final VarHandle EnumDevices$VH = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("EnumDevices"));

  public static EnumDevices EnumDevices(MemorySegment vtableSegment, MemorySession session) {
    // This looks ugly but it's necessary because we want to dereference the pointer contained by the virtual table
    var vtblRef = vtableSegment.get(ADDRESS, 0);
    var vtblSeg = MemorySegment.ofAddress(vtblRef, $LAYOUT.byteSize(), session);
    return EnumDevices.ofAddress((MemoryAddress) EnumDevices$VH.get(vtblSeg));
  }

  public interface EnumDevices {

    int apply(Addressable _x0, int _x1, Addressable _x2, Addressable _x3, int _x4);

    static EnumDevices ofAddress(MemoryAddress address) {
      return (Addressable __x0, int __x1, Addressable __x2, Addressable __x3, int __x4) -> {
        try {
          var EnumDevices$MH = RuntimeHelper.downcallHandle(address, IDirectInput8WVtbl.EnumDevices$FUNC);
          return (int) EnumDevices$MH.invokeExact(__x0, __x1, __x2, __x3, __x4);
        } catch (Throwable ex$) {
          throw new AssertionError("should not reach here", ex$);
        }
      };
    }
  }
}
