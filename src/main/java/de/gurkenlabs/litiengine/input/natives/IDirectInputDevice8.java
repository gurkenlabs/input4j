package de.gurkenlabs.litiengine.input.natives;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

class IDirectInputDevice8 {
  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          ADDRESS.withName("lpVtbl")
  ).withName("IDirectInputDevice8A");

  private static final VarHandle lpVtbl$VH = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lpVtbl"));

  public MemorySegment vtable;
  final DIDEVICEINSTANCE deviceInstance;

  private MemorySegment vtablePointerSegment;

  IDirectInputDevice8(DIDEVICEINSTANCE deviceInstance) {
    this.deviceInstance = deviceInstance;
  }

  public void create(MemorySegment segment, MemorySession memorySession) {
    var pointer = (MemoryAddress) lpVtbl$VH.get(segment);

    this.vtablePointerSegment = MemorySegment.ofAddress(pointer, IDirectInputDevice8.Vtable.$LAYOUT.byteSize(), memorySession);

    // Dereference the pointer for the memory segment of the virtual table
    this.vtable = MemorySegment.ofAddress(this.vtablePointerSegment.get(ADDRESS, 0), IDirectInputDevice8.Vtable.$LAYOUT.byteSize(), memorySession);

    // init API method handles
  }

  static class Vtable {
    static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
            ADDRESS.withName("QueryInterface"),
            ADDRESS.withName("AddRef"),
            ADDRESS.withName("Release"),
            ADDRESS.withName("GetCapabilities"),
            ADDRESS.withName("EnumObjects"),
            ADDRESS.withName("GetProperty"),
            ADDRESS.withName("SetProperty"),
            ADDRESS.withName("Acquire"),
            ADDRESS.withName("Unacquire"),
            ADDRESS.withName("GetDeviceState"),
            ADDRESS.withName("GetDeviceData"),
            ADDRESS.withName("SetDataFormat"),
            ADDRESS.withName("SetEventNotification"),
            ADDRESS.withName("SetCooperativeLevel"),
            ADDRESS.withName("GetObjectInfo"),
            ADDRESS.withName("GetDeviceInfo"),
            ADDRESS.withName("RunControlPanel"),
            ADDRESS.withName("Initialize"),
            ADDRESS.withName("CreateEffect"),
            ADDRESS.withName("EnumEffects"),
            ADDRESS.withName("GetEffectInfo"),
            ADDRESS.withName("GetForceFeedbackState"),
            ADDRESS.withName("SendForceFeedbackCommand"),
            ADDRESS.withName("EnumCreatedEffectObjects"),
            ADDRESS.withName("Escape"),
            ADDRESS.withName("Poll"),
            ADDRESS.withName("SendDeviceData"),
            ADDRESS.withName("EnumEffectsInFile"),
            ADDRESS.withName("WriteEffectToFile"),
            ADDRESS.withName("BuildActionMap"),
            ADDRESS.withName("SetActionMap"),
            ADDRESS.withName("GetImageInfo")
    ).withName("IDirectInputDevice8AVtbl");
  }
}
