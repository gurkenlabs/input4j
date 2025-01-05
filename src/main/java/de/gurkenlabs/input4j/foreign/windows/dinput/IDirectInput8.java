package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

final class IDirectInput8 {
  static GUID IID_IDirectInput8W = new GUID(0xBF798031, (short) 0x483A, (short) 0x4DA2, (byte) 0xAA, (byte) 0x99, (byte) 0x5D, (byte) 0x64, (byte) 0xED, (byte) 0x36, (byte) 0x97, (byte) 0x00);

  static final int DIEDFL_ALLDEVICES = 0x00000000;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ADDRESS.withName("lpVtbl")
  ).withName("IDirectInput8W");

  private static final VarHandle VH_lpVtbl = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lpVtbl"));

  public MemorySegment vtable;

  private MemorySegment vtablePointerSegment;

  private MethodHandle enumDevices;
  private MethodHandle createDevice;

  public static IDirectInput8 read(MemorySegment segment, Arena memoryArena) {
    var directInput = new IDirectInput8();
    var pointer = (MemorySegment) VH_lpVtbl.get(segment, 0);

    directInput.vtablePointerSegment = MemorySegment.ofAddress(pointer.address()).reinterpret(IDirectInput8.$LAYOUT.byteSize(), memoryArena, null);

    // Dereference the pointer for the memory segment of the virtual table
    directInput.vtable = MemorySegment.ofAddress(directInput.vtablePointerSegment.get(ADDRESS, 0).address()).reinterpret(Vtable.$LAYOUT.byteSize(), memoryArena, null);

    // init API method handles
    var enumDevicesPointer = (MemorySegment) Vtable.VH_EnumDevices.get(directInput.vtable, 0);
    directInput.enumDevices = downcallHandle(enumDevicesPointer, Vtable.enumDevicesDescriptor);

    var createDevicePointer = (MemorySegment) Vtable.VH_CreateDevice.get(directInput.vtable, 0);
    directInput.createDevice = downcallHandle(createDevicePointer, Vtable.createDeviceDescriptor);

    return directInput;
  }

  /**
   * Enumerates available devices.
   *
   * @param dwDevType  Device type filter.
   *                   To restrict the enumeration to a particular type of device, set this parameter to a {@link DI8DEVTYPE} value.
   * @param lpCallback Address of a callback function to be called once for each device enumerated.
   * @param dwFlags    Flag value that specifies the scope of the enumeration (e.g. {@code DIEDFL_ALLDEVICES}).
   * @return If the method succeeds, the return value is DI_OK. If the method fails, the return value can be one of the
   * following error values: DIERR_INVALIDPARAM, DIERR_NOTINITIALIZED.
   * @throws Throwable If the native invokation fails this can throw
   */
  public int EnumDevices(int dwDevType, MemorySegment lpCallback, int dwFlags) throws Throwable {
    return (int) enumDevices.invokeExact(this.vtablePointerSegment, dwDevType, lpCallback, MemorySegment.NULL, dwFlags);
  }

  /**
   * Creates and initializes an instance of a device based on a given globally unique identifier (GUID), and obtains an IDirectInputDevice8 Interface interface.
   *
   * @param rguid                 Reference to the GUID for the desired input device.
   * @param lplpDirectInputDevice Address of a variable to receive the IDirectInputDevice8 interface pointer if successful.
   * @return If the method succeeds, the return value is DI_OK. If the method fails, the return value can be one of the
   * following: DIERR_DEVICENOTREG, DIERR_INVALIDPARAM, DIERR_NOINTERFACE, DIERR_NOTINITIALIZED, DIERR_OUTOFMEMORY.
   * @throws Throwable If the native invokation fails this can throw
   */
  public int CreateDevice(MemorySegment rguid, MemorySegment lplpDirectInputDevice) throws Throwable {
    return (int) createDevice.invokeExact(this.vtablePointerSegment, rguid, lplpDirectInputDevice, MemorySegment.NULL);
  }

  public void Release(){
    // TODO: Release when DirectInputPlugin is closed
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
