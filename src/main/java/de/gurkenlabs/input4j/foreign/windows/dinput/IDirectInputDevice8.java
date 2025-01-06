package de.gurkenlabs.input4j.foreign.windows.dinput;

import de.gurkenlabs.input4j.InputDevice;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.List;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.*;

final class IDirectInputDevice8 {
  public final static int DIDFT_AXIS = 0x00000003;
  public final static int DIDFT_BUTTON = 0x0000000C;

  public final static int DIDFT_POV = 0x00000010;

  public final static int DIDFT_RELAXIS = 0x00000001;

  public final static int DIDF_ABSAXIS = 0x00000001;
  public final static int DIDF_RELAXIS = 0x00000002;

  /**
   * Exclusive cooperative level. The application has exclusive access to the device.
   * Other applications cannot acquire the device while it is acquired at this level.
   */
  public final static int DISCL_EXCLUSIVE = 0x00000001;

  /**
   * Non-exclusive cooperative level. The application shares access to the device with other applications.
   * Multiple applications can acquire the device at this level.
   */
  public final static int DISCL_NONEXCLUSIVE = 0x00000002;

  /**
   * Foreground cooperative level. The device is acquired only when the application is in the foreground.
   * The device is automatically unacquired when the application moves to the background.
   */
  public final static int DISCL_FOREGROUND = 0x00000004;

  /**
   * Background cooperative level. The device can be acquired even when the application is in the background.
   * This allows the application to continue receiving input while not in focus.
   */
  public final static int DISCL_BACKGROUND = 0x00000008;

  /**
   * No Windows key cooperative level. The Windows key is disabled while the device is acquired.
   * This prevents the user from accidentally switching out of the application.
   */
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
    var pointer = (MemorySegment) VH_lpVtbl.get(segment, 0);

    this.vtablePointerSegment = MemorySegment.ofAddress(pointer.address()).reinterpret(IDirectInputDevice8.$LAYOUT.byteSize(), memoryArena, null);

    // Dereference the pointer for the memory segment of the virtual table
    this.vtable = MemorySegment.ofAddress(this.vtablePointerSegment.get(ADDRESS, 0).address()).reinterpret(Vtable.$LAYOUT.byteSize(), memoryArena, null);

    // init API method handles
    var enumDevicesPointer = (MemorySegment) Vtable.VH_EnumObjects.get(this.vtable, 0);
    this.enumObjects = downcallHandle(enumDevicesPointer, Vtable.enumObjectsDescriptor);

    var acquirePointer = (MemorySegment) Vtable.VH_Acquire.get(this.vtable, 0);
    this.acquire = downcallHandle(acquirePointer, Vtable.acquireDescriptor);

    var unacquirePointer = (MemorySegment) Vtable.VH_Unacquire.get(this.vtable, 0);
    this.unacquire = downcallHandle(unacquirePointer, Vtable.unacquireDescriptor);

    var pollPointer = (MemorySegment) Vtable.VH_Poll.get(this.vtable, 0);
    this.poll = downcallHandle(pollPointer, Vtable.pollDescriptor);

    var setDataFormatPointer = (MemorySegment) Vtable.VH_SetDataFormat.get(this.vtable, 0);
    this.setDataFormat = downcallHandle(setDataFormatPointer, Vtable.setDataFormatDescriptor);

    var setCooperativeLevelPointer = (MemorySegment) Vtable.VH_SetCooperativeLevel.get(this.vtable, 0);
    this.setCooperativeLevel = downcallHandle(setCooperativeLevelPointer, Vtable.setCooperativeLevelDescriptor);

    var setPropertyPointer = (MemorySegment) Vtable.VH_SetProperty.get(this.vtable, 0);
    this.setProperty = downcallHandle(setPropertyPointer, Vtable.setPropertyDescriptor);

    var getDeviceStatePointer = (MemorySegment) Vtable.VH_GetDeviceState.get(this.vtable, 0);
    this.getDeviceState = downcallHandle(getDeviceStatePointer, Vtable.getDeviceStateDescriptor);

    var getDeviceDataPointer = (MemorySegment) Vtable.VH_GetDeviceData.get(this.vtable, 0);
    this.getDeviceData = downcallHandle(getDeviceDataPointer, Vtable.getDeviceDataDescriptor);

    var getPropertyPointer = (MemorySegment) Vtable.VH_GetProperty.get(this.vtable, 0);
    this.getProperty = downcallHandle(getPropertyPointer, Vtable.getPropertyDescriptor);
  }

  /**
   * Enumerates the objects associated with the device (such as buttons or axes) and invokes the provided callback.
   *
   * @param lpCallback A {@link MemorySegment} containing a pointer to the callback function that will be invoked for each device object.
   * @param dwFlags Flags that control how the enumeration of objects is done.
   * @return An integer indicating the result of the operation. Non-zero values indicate errors.
   * @throws Throwable If an exception occurs while enumerating the objects.
   */
  public int EnumObjects(MemorySegment lpCallback, int dwFlags) throws Throwable {
    return (int) enumObjects.invokeExact(this.vtablePointerSegment, lpCallback, MemorySegment.NULL, dwFlags);
  }

  /**
   * Acquires the device, which allows the application to receive input from the device.
   * This method must be called before polling or receiving input data from the device.
   *
   * @return If the method succeeds, the return value is {@link Result#DI_OK}.
   * If the method fails, the return value can be one of the following error values:
   * {@link Result#DIERR_INVALIDPARAM}, {@link Result#DIERR_NOTINITIALIZED}, {@link Result#DIERR_OTHERAPPHASPRIO}
   * @throws Throwable If an exception occurs while acquiring the device.
   */
  public int Acquire() throws Throwable {
    return (int) acquire.invokeExact(this.vtablePointerSegment);
  }

  /**
   * Releases the device, making it stop accepting input.
   *
   * @throws Throwable If an exception occurs while unacquiring the device.
   */
  public void Unacquire() throws Throwable {
    unacquire.invokeExact(this.vtablePointerSegment);
  }

  /**
   * Polls the device to retrieve the latest input data.
   * This method is typically called in a loop to continually fetch the input state from the device.
   *
   * @return A {@code int} indicating the result of the operation. A value of {@link Result#DI_OK} indicates success.
   *         Error codes such as {@link Result#DIERR_INPUTLOST} or {@link Result#DIERR_NOTACQUIRED} may be returned.
   * @throws Throwable If an exception occurs while polling the device.
   */
  public int Poll() throws Throwable {
    return (int) poll.invokeExact(this.vtablePointerSegment);
  }

  /**
   * Sets the data format for the device, which defines the structure of the data returned by the device.
   * This method is called to specify the type and layout of the input data.
   *
   * @param lpdf A {@link MemorySegment} containing a pointer to a {@link DIDATAFORMAT} structure that defines the data format.
   * @return A {@code int} indicating the result of the operation. A value of {@link Result#DI_OK} indicates success.
   *         Possible error codes include {@link Result#DIERR_INVALIDPARAM} if the format is incorrect.
   * @throws Throwable If an exception occurs while setting the data format.
   */
  public int SetDataFormat(MemorySegment lpdf) throws Throwable {
    return (int) setDataFormat.invokeExact(this.vtablePointerSegment, lpdf);
  }

  /**
   * Sets the cooperative level for the device, which defines how the device interacts with other applications.
   * The cooperative level also determines the device's access to input data (exclusive vs. shared access).
   *
   * @param hwnd A {@code long} representing the window handle (HWND) of the application acquiring the device.
   * @param dwFlags Flags specifying the cooperative level, such as:
   *                {@link #DISCL_FOREGROUND}, {@link #DISCL_BACKGROUND}, and {@link #DISCL_EXCLUSIVE}.
   *                These flags determine whether the device is accessed exclusively or shared with other applications.
   * @return A {@code int} indicating the result of the operation. A value of {@link Result#DI_OK} indicates success.
   *         Possible error codes include {@link Result#DIERR_INVALIDPARAM} or {@link Result#DIERR_NOTACQUIRED}.
   * @throws Throwable If an exception occurs while setting the cooperative level.
   */
  public int SetCooperativeLevel(long hwnd, int dwFlags) throws Throwable {
    return (int) setCooperativeLevel.invokeExact(this.vtablePointerSegment, hwnd, dwFlags);
  }

  /**
   * Sets a property for the device, such as sensitivity or other device-specific properties.
   * This method allows modifying configuration parameters of the device.
   *
   * @param rguidProp A {@link MemorySegment} containing the GUID of the property to set.
   * @param pdiph A {@link MemorySegment} containing the property data.
   * @return A {@code int} indicating the result of the operation. A value of {@link Result#DI_OK} indicates success.
   *         Possible error codes include {@link Result#DIERR_INVALIDPARAM} or others.
   * @throws Throwable If an exception occurs while setting the property.
   */
  public int SetProperty(MemorySegment rguidProp, MemorySegment pdiph) throws Throwable {
    return (int) setProperty.invokeExact(this.vtablePointerSegment, rguidProp, pdiph);
  }

  /**
   * Retrieves the current state of the device, including the state of buttons, axes, and other input controls.
   * This method provides a snapshot of the device state.
   *
   * @param cbData The size of the buffer that will hold the device state data.
   * @param lpvData A {@link MemorySegment} that receives the device state data, typically structured as a series of input events.
   * @return A {@code int} indicating the result of the operation. A value of {@link Result#DI_OK} indicates success.
   *         Error codes such as {@link Result#DIERR_INPUTLOST} or {@link Result#DIERR_NOTACQUIRED} may be returned.
   * @throws Throwable If an exception occurs while getting the device state.
   */
  public int GetDeviceState(int cbData, MemorySegment lpvData) throws Throwable {
    return (int) getDeviceState.invokeExact(this.vtablePointerSegment, cbData, lpvData);
  }

  /**
   * Retrieves input data from the device, such as button presses or axis movements.
   * This method processes and stores the input events in the specified buffer.
   *
   * @param rgdod A {@link MemorySegment} containing a structure that will hold the device input data.
   * @param pdwInOut A {@link MemorySegment} specifying the size of the input data buffer.
   * @return A {@code int} indicating the result of the operation. A value of {@link Result#DI_OK} indicates success.
   *         Possible error codes include {@link Result#DIERR_INPUTLOST}, {@link Result#DIERR_NOTACQUIRED}, or others.
   * @throws Throwable If an exception occurs while retrieving the device data.
   */
  public int GetDeviceData(MemorySegment rgdod, MemorySegment pdwInOut) throws Throwable {
    return (int) getDeviceData.invokeExact(this.vtablePointerSegment, (int) DIDEVICEOBJECTDATA.$LAYOUT.byteSize(), rgdod, pdwInOut, 0);
  }

  /**
   * Retrieves a property from the device, such as configuration settings.
   * This method fetches the current configuration or state of a specific property.
   *
   * @param rguidProp A {@link MemorySegment} containing the GUID of the property to retrieve.
   * @param pdiph A {@link MemorySegment} that will hold the retrieved property data.
   * @return A {@code int} indicating the result of the operation. A value of {@link Result#DI_OK} indicates success.
   *         Possible error codes include {@link Result#DIERR_INVALIDPARAM} or others.
   * @throws Throwable If an exception occurs while getting the property.
   */
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
    private static final FunctionDescriptor setCooperativeLevelDescriptor = FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_LONG, JAVA_INT);
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
