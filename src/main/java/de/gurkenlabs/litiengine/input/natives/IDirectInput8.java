package de.gurkenlabs.litiengine.input.natives;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

final class IDirectInput8 {
  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ADDRESS.withName("lpVtbl")
  ).withName("IDirectInput8W");

  private static final VarHandle lpVtbl$VH = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lpVtbl"));

  public MemorySegment vtable;

  private MemorySegment vtablePointerSegment;

  private MethodHandle enumDevices;
  private MethodHandle createDevice;

  public static IDirectInput8 read(MemorySegment segment, MemorySession memorySession) {
    var directInput = new IDirectInput8();
    var pointer = (MemoryAddress) lpVtbl$VH.get(segment);

    directInput.vtablePointerSegment = MemorySegment.ofAddress(pointer, Vtable.$LAYOUT.byteSize(), memorySession);

    // Dereference the pointer for the memory segment of the virtual table
    directInput.vtable = MemorySegment.ofAddress(directInput.vtablePointerSegment.get(ADDRESS, 0), Vtable.$LAYOUT.byteSize(), memorySession);

    // init API method handles
    var enumDevicesPointer = (MemoryAddress) Vtable.VH_EnumDevices.get(directInput.vtable);
    directInput.enumDevices = RuntimeHelper.downcallHandle(enumDevicesPointer, Vtable.enumDevicesDescriptor);

    var createDevicePointer = (MemoryAddress) Vtable.VH_CreateDevice.get(directInput.vtable);
    directInput.createDevice = RuntimeHelper.downcallHandle(createDevicePointer, Vtable.createDeviceDescriptor);

    return directInput;
  }

  public int EnumDevices(int dwDevType, Addressable lpCallback, Addressable pvRef, int dwFlags) throws Throwable {
    return (int) enumDevices.invokeExact((Addressable) this.vtablePointerSegment, dwDevType, lpCallback, pvRef, dwFlags);
  }

  public int CreateDevice(Addressable rguid, Addressable lplpDirectInputDevice) throws Throwable {
    return (int) createDevice.invokeExact((Addressable) this.vtablePointerSegment, rguid, lplpDirectInputDevice, (Addressable)MemoryAddress.NULL);
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

    private static final FunctionDescriptor enumDevicesDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT);

    private static final VarHandle VH_EnumDevices = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("EnumDevices"));

    private static final FunctionDescriptor createDeviceDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS);

    private static final VarHandle VH_CreateDevice = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("CreateDevice"));
  }
}
