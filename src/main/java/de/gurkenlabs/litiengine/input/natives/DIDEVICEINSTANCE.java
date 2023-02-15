package de.gurkenlabs.litiengine.input.natives;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 * Describes an instance of a DirectInput device.
 * This structure is used with the IDirectInput8::EnumDevices, IDirectInput8::EnumDevicesBySemantics,
 * and IDirectInputDevice8::GetDeviceInfo methods.
 */
final class DIDEVICEINSTANCE {
  static final int MAX_PATH = 260;

  /**
   * Size of this structure, in bytes. This member must be initialized before the structure is used.
   */
  public int dwSize = (int) $LAYOUT.byteSize();

  /**
   * Unique identifier for the instance of the device. An application can save the instance globally unique identifier
   * (GUID) into a configuration file and use it at a later time. Instance GUIDs are specific to a particular computer.
   * An instance GUID obtained from one computer is unrelated to instance GUIDs on another.
   */
  public GUID guidInstance;

  /**
   * Unique identifier for the product. This identifier is established by the manufacturer of the device.
   */
  public GUID guidProduct;

  /**
   * Device type specifier. The least-significant byte of the device type description code specifies the device type.
   * The next-significant byte specifies the device subtype.
   * This value can also be combined with DIDEVTYPE_HID, which specifies a Human Interface Device (human interface device).
   */
  public int dwDevType;

  /**
   * Friendly name for the instance. For example, "Joystick 1."
   */
  public char[] tszInstanceName = new char[MAX_PATH];

  /**
   * Friendly name for the product.
   */
  public char[] tszProductName = new char[MAX_PATH];

  /**
   * Unique identifier for the driver being used for force feedback.
   * The driver's manufacturer establishes this identifier.
   */
  public GUID guidFFDriver;

  /**
   * If the device is a Human Interface Device (HID), this member contains the HID usage page code.
   * <ul>
   *  <li>0x01	Generic Desktop Controls</li>
   *  <lI>0x05	Game Controls</lI>
   *  <lI>0x08	LEDs</lI>
   *  <lI>0x09	Button</lI>
   * </ul>
   */
  public short wUsagePage;

  /**
   * If the device is a Human Interface Device (HID), this member contains the HID usage code.
   * <ul>
   *  <li>0x01	Pointer</li>
   *  <li>0x02	Mouse</li>
   *  <li>0x04	Joystick</li>
   *  <li>0x05	Game Pad</li>
   *  <li>0x06	Keyboard</li>
   *  <li>0x07	Keypad</li>
   *  <li>0x08	Multi-axis Controller</li>
   * </ul>
   */
  public short wUsage;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          ValueLayout.JAVA_INT.withName("dwSize"),
          GUID.$LAYOUT.withName("guidInstance"),
          GUID.$LAYOUT.withName("guidProduct"),
          ValueLayout.JAVA_INT.withName("dwDevType"),
          MemoryLayout.sequenceLayout(MAX_PATH, ValueLayout.JAVA_CHAR).withName("tszInstanceName"),
          MemoryLayout.sequenceLayout(MAX_PATH, ValueLayout.JAVA_CHAR).withName("tszProductName"),
          GUID.$LAYOUT.withName("guidFFDriver"),
          ValueLayout.JAVA_SHORT.withName("wUsagePage"),
          ValueLayout.JAVA_SHORT.withName("wUsage")
  ).withName("DIDEVICEINSTANCE");

  static final VarHandle VH_dwSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSize"));
  static final VarHandle VH_dwDevType = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwDevType"));
  static final VarHandle VH_tszInstanceName = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("tszInstanceName"), MemoryLayout.PathElement.sequenceElement());
  static final VarHandle VH_tszProductName = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("tszProductName"), MemoryLayout.PathElement.sequenceElement());
  static final VarHandle VH_wUsagePage = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wUsagePage"));
  static final VarHandle VH_wUsage = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("wUsage"));

  public static DIDEVICEINSTANCE read(MemorySegment segment) {
    var data = new DIDEVICEINSTANCE();
    data.dwSize = (int) VH_dwSize.get(segment);
    // ensure the offset of the cbSize integer before reading the guid
    data.guidInstance = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));
    data.guidProduct = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize() + GUID.$LAYOUT.byteSize()));
    data.dwDevType = (int) VH_dwDevType.get(segment);

    char[] tszInstanceName = new char[MAX_PATH];
    for (int i = 0; i < MAX_PATH; i++) {
      tszInstanceName[i] = (char) VH_tszInstanceName.get(segment, i);
    }

    data.tszInstanceName = tszInstanceName;

    char[] tszProductName = new char[MAX_PATH];
    for (int i = 0; i < MAX_PATH; i++) {
      tszProductName[i] = (char) VH_tszProductName.get(segment, i);
    }

    data.tszProductName = tszProductName;
    data.guidFFDriver = GUID.read(segment.asSlice(ValueLayout.JAVA_INT.byteSize() + GUID.$LAYOUT.byteSize() + GUID.$LAYOUT.byteSize() + ValueLayout.JAVA_INT.byteSize() + MAX_PATH + MAX_PATH));
    data.wUsagePage = (short) VH_wUsagePage.get(segment);
    data.wUsage = (short) VH_wUsage.get(segment);
    return data;
  }

  public void write(MemorySegment segment) {
    VH_dwSize.set(segment, dwSize);
    if (guidInstance != null) {
      guidInstance.write(segment.asSlice(ValueLayout.JAVA_INT.byteSize()));
    }

    if (guidProduct != null) {
      guidProduct.write(segment.asSlice(ValueLayout.JAVA_INT.byteSize() + GUID.$LAYOUT.byteSize()));
    }

    VH_dwDevType.set(segment, dwDevType);

    for (int i = 0; i < tszInstanceName.length; i++) {
      VH_tszInstanceName.set(segment, i, tszInstanceName[i]);
    }

    for (int i = 0; i < tszProductName.length; i++) {
      VH_tszProductName.set(segment, i, tszProductName[i]);
    }

    if (guidFFDriver != null) {
      guidFFDriver.write(segment.asSlice(ValueLayout.JAVA_INT.byteSize() + GUID.$LAYOUT.byteSize() + GUID.$LAYOUT.byteSize() + ValueLayout.JAVA_INT.byteSize() + MAX_PATH + MAX_PATH));
    }

    VH_wUsagePage.set(segment, wUsagePage);
    VH_wUsage.set(segment, wUsage);
  }
}
