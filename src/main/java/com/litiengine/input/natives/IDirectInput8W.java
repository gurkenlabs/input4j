package com.litiengine.input.natives;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class IDirectInput8W {


  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ADDRESS.withName("lpVtbl")
  ).withName("IDirectInput8W");

  static final VarHandle lpVtbl$VH = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lpVtbl"));
  public MemorySegment _self;
  public MemoryAddress pointer;

  public MemorySegment vtable;

  public static IDirectInput8W read(MemorySegment segment, MemorySession memorySession) {
    var directInput = new IDirectInput8W();
    directInput._self = segment;
    directInput.pointer = (MemoryAddress)lpVtbl$VH.get(segment);

    directInput.vtable = MemorySegment.ofAddress(directInput.pointer, IDirectInput8WVtbl.$LAYOUT.byteSize(), memorySession);
    return directInput;
  }
}
