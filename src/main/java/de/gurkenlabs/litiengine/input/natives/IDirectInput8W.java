package de.gurkenlabs.litiengine.input.natives;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static de.gurkenlabs.litiengine.input.natives.IDirectInput8W.Vtable.EnumDevices$VH;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

final class IDirectInput8W {
  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ADDRESS.withName("lpVtbl")
  ).withName("IDirectInput8W");

  static final VarHandle lpVtbl$VH = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lpVtbl"));
  public MemorySegment _self;

  public MemorySegment vtable;

  private MemorySegment vtablePointerSegment;

  public static IDirectInput8W read(MemorySegment segment, MemorySession memorySession) {
    var directInput = new IDirectInput8W();
    directInput._self = segment;
    var pointer = (MemoryAddress) lpVtbl$VH.get(segment);

    directInput.vtablePointerSegment = MemorySegment.ofAddress(pointer, Vtable.$LAYOUT.byteSize(), memorySession);

    // Dereference the pointer for the memory segment of the virtual table
    directInput.vtable = MemorySegment.ofAddress(directInput.vtablePointerSegment.get(ADDRESS, 0), Vtable.$LAYOUT.byteSize(), memorySession);
    return directInput;
  }

  public int EnumDevices(int dwDevType, Addressable lpCallback, Addressable pvRef, int dwFlags) {
    var enumDevices = Vtable.EnumDevices.ofAddress((MemoryAddress) EnumDevices$VH.get(vtable));
    return enumDevices.apply(this.vtablePointerSegment, dwDevType, lpCallback, pvRef, dwFlags);
  }

  static class Vtable {
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


    public interface EnumDevices {

      int apply(Addressable _x0, int _x1, Addressable _x2, Addressable _x3, int _x4);

      static EnumDevices ofAddress(MemoryAddress address) {
        return (Addressable __x0, int __x1, Addressable __x2, Addressable __x3, int __x4) -> {
          try {
            var EnumDevices$MH = RuntimeHelper.downcallHandle(address, Vtable.EnumDevices$FUNC);
            return (int) EnumDevices$MH.invokeExact(__x0, __x1, __x2, __x3, __x4);
          } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
          }
        };
      }
    }
  }
}
