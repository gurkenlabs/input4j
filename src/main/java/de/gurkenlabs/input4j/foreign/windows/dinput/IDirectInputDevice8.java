package de.gurkenlabs.input4j.foreign.windows.dinput;

import de.gurkenlabs.input4j.InputDevice;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.List;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

final class IDirectInputDevice8 {
  public final static int DIDFT_AXIS = 0x00000003;
  public final static int DIDFT_BUTTON = 0x0000000C;

  public final static int DIDFT_POV = 0x00000010;

  public final static int DIDFT_RELAXIS = 0x00000001;

  public final static int DIDF_ABSAXIS = 0x00000001;
  public final static int DIDF_RELAXIS = 0x00000002;

  public final static int DISCL_EXCLUSIVE = 0x00000001;
  public final static int DISCL_NONEXCLUSIVE = 0x00000002;
  public final static int DISCL_FOREGROUND = 0x00000004;
  public final static int DISCL_BACKGROUND = 0x00000008;
  public final static int DISCL_NOWINKEY = 0x00000010;

  final static int DIPROPRANGE_NOMIN = -2147483648;

  final static int DIPROPRANGE_NOMAX = 2147483647;

  final static MemorySegment DIPROP_BUFFERSIZE = MemorySegment.ofAddress(1L);
  static final MemorySegment DIPROP_AXISMODE = MemorySegment.ofAddress(2L);
  static final MemorySegment DIPROP_GRANULARITY = MemorySegment.ofAddress(3L);
  static final MemorySegment DIPROP_RANGE = MemorySegment.ofAddress(4L);
  static final MemorySegment DIPROP_DEADZONE = MemorySegment.ofAddress(5L);
  static final MemorySegment DIPROP_SATURATION = MemorySegment.ofAddress(6L);
  static final MemorySegment DIPROP_FFGAIN = MemorySegment.ofAddress(7L);

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          ADDRESS.withName("lpVtbl")
  ).withName("IDirectInputDevice8A");

  private static final VarHandle VH_lpVtbl = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lpVtbl"));

  public MemorySegment vtable;
  final DIDEVICEINSTANCE deviceInstance;

  List<DIDEVICEOBJECTINSTANCE> deviceObjects;

  final InputDevice inputDevice;

  private MemorySegment vtablePointerSegment;

  private MethodHandle enumObjects;

  private MethodHandle acquire;

  private MethodHandle unacquire;

  private MethodHandle poll;

  private MethodHandle setDataFormat;

  private MethodHandle setCooperativeLevel;

  private MethodHandle setProperty;

  private MethodHandle getDeviceState;

  private MethodHandle getDeviceData;

  private MethodHandle getProperty;

  IDirectInputDevice8(DIDEVICEINSTANCE deviceInstance, InputDevice inputDevice) {
    this.deviceInstance = deviceInstance;
    this.inputDevice = inputDevice;
  }

  public void create(MemorySegment segment, Arena memoryArena) {
    var pointer = (MemorySegment) VH_lpVtbl.get(segment);

    this.vtablePointerSegment = MemorySegment.ofAddress(pointer.address()).reinterpret(IDirectInputDevice8.$LAYOUT.byteSize(), memoryArena, null);

    // Dereference the pointer for the memory segment of the virtual table
    this.vtable = MemorySegment.ofAddress(this.vtablePointerSegment.get(ADDRESS, 0).address()).reinterpret(Vtable.$LAYOUT.byteSize(), memoryArena, null);

    // init API method handles
    var enumDevicesPointer = (MemorySegment) Vtable.VH_EnumObjects.get(this.vtable);
    this.enumObjects = downcallHandle(enumDevicesPointer, Vtable.enumObjectsDescriptor);

    var acquirePointer = (MemorySegment) Vtable.VH_Acquire.get(this.vtable);
    this.acquire = downcallHandle(acquirePointer, Vtable.acquireDescriptor);

    var unacquirePointer = (MemorySegment) Vtable.VH_Unacquire.get(this.vtable);
    this.unacquire = downcallHandle(unacquirePointer, Vtable.unacquireDescriptor);

    var pollPointer = (MemorySegment) Vtable.VH_Poll.get(this.vtable);
    this.poll = downcallHandle(pollPointer, Vtable.pollDescriptor);

    var setDataFormatPointer = (MemorySegment) Vtable.VH_SetDataFormat.get(this.vtable);
    this.setDataFormat = downcallHandle(setDataFormatPointer, Vtable.setDataFormatDescriptor);

    var setCooperativeLevelPointer = (MemorySegment) Vtable.VH_SetCooperativeLevel.get(this.vtable);
    this.setCooperativeLevel = downcallHandle(setCooperativeLevelPointer, Vtable.setCooperativeLevelDescriptor);

    var setPropertyPointer = (MemorySegment) Vtable.VH_SetProperty.get(this.vtable);
    this.setProperty = downcallHandle(setPropertyPointer, Vtable.setPropertyDescriptor);

    var getDeviceStatePointer = (MemorySegment) Vtable.VH_GetDeviceState.get(this.vtable);
    this.getDeviceState = downcallHandle(getDeviceStatePointer, Vtable.getDeviceStateDescriptor);

    var getDeviceDataPointer = (MemorySegment) Vtable.VH_GetDeviceData.get(this.vtable);
    this.getDeviceData = downcallHandle(getDeviceDataPointer, Vtable.getDeviceDataDescriptor);

    var getPropertyPointer = (MemorySegment) Vtable.VH_GetProperty.get(this.vtable);
    this.getProperty = downcallHandle(getPropertyPointer, Vtable.getPropertyDescriptor);
  }

  public int EnumObjects(MemorySegment lpCallback, int dwFlags) throws Throwable {
    return (int) enumObjects.invokeExact(this.vtablePointerSegment, lpCallback, MemorySegment.NULL, dwFlags);
  }

  public int Acquire() throws Throwable {
    return (int) acquire.invokeExact(this.vtablePointerSegment);
  }

  public void Unacquire() throws Throwable {
    unacquire.invokeExact(this.vtablePointerSegment);
  }

  public int Poll() throws Throwable {
    return (int) poll.invokeExact(this.vtablePointerSegment);
  }

  public int SetDataFormat(MemorySegment lpdf) throws Throwable {
    return (int) setDataFormat.invokeExact(this.vtablePointerSegment, lpdf);
  }

  public int SetCooperativeLevel(MemorySegment hwnd, int dwFlags) throws Throwable {
    return (int) setCooperativeLevel.invokeExact(this.vtablePointerSegment, hwnd, dwFlags);
  }

  public int SetProperty(MemorySegment rguidProp, MemorySegment pdiph) throws Throwable {
    return (int) setProperty.invokeExact(this.vtablePointerSegment, rguidProp, pdiph);
  }

  public int GetDeviceState(int cbData, MemorySegment lpvData) throws Throwable {
    return (int) getDeviceState.invokeExact(this.vtablePointerSegment, cbData, lpvData);
  }

  public int GetDeviceData(MemorySegment rgdod, MemorySegment pdwInOut) throws Throwable {
    return (int) getDeviceData.invokeExact(this.vtablePointerSegment, (int) DIDEVICEOBJECTDATA.$LAYOUT.byteSize(), rgdod, pdwInOut, 0);
  }

  public int GetProperty(MemorySegment rguidProp, MemorySegment pdiph) throws Throwable {
    return (int) getProperty.invokeExact(this.vtablePointerSegment, rguidProp, pdiph);
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

    private static final FunctionDescriptor enumObjectsDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, JAVA_INT);
    private static final FunctionDescriptor acquireDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);
    private static final FunctionDescriptor unacquireDescriptor = FunctionDescriptor.ofVoid(ADDRESS);
    private static final FunctionDescriptor pollDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS);
    private static final FunctionDescriptor setDataFormatDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS);
    private static final FunctionDescriptor setCooperativeLevelDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT);
    private static final FunctionDescriptor setPropertyDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS);
    private static final FunctionDescriptor getDeviceStateDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS);
    private static final FunctionDescriptor getDeviceDataDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT);
    private static final FunctionDescriptor getPropertyDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS);

    private static final VarHandle VH_EnumObjects = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("EnumObjects"));
    private static final VarHandle VH_Acquire = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Acquire"));
    private static final VarHandle VH_Unacquire = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Unacquire"));
    private static final VarHandle VH_Poll = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("Poll"));
    private static final VarHandle VH_SetDataFormat = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("SetDataFormat"));
    private static final VarHandle VH_SetCooperativeLevel = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("SetCooperativeLevel"));
    private static final VarHandle VH_SetProperty = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("SetProperty"));
    private static final VarHandle VH_GetDeviceState = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("GetDeviceState"));
    private static final VarHandle VH_GetDeviceData = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("GetDeviceData"));
    private static final VarHandle VH_GetProperty = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("GetProperty"));
  }
}
