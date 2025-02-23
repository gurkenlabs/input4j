package de.gurkenlabs.input4j.foreign.macos.iokit;

import de.gurkenlabs.input4j.InputDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an IOHIDDevice, which is a Human Interface Device (HID) on macOS.
 * This class encapsulates the properties and elements of an HID device.
 */
class IOHIDDevice {
  /**
   * The list of elements (controls) associated with this HID device.
   */
  private final List<IOHIDElement> elements = new ArrayList<>();

  /**
   * The memory address of the HID device.
   * Corresponds to the `IOHIDDeviceRef` in native C.
   */
  long address;

  /**
   * The vendor ID of the HID device.
   * Corresponds to the `kIOHIDVendorIDKey` in native C.
   */
  int vendorId;

  /**
   * The product ID of the HID device.
   * Corresponds to the `kIOHIDProductIDKey` in native C.
   */
  int productId;

  /**
   * The product name of the HID device.
   * Corresponds to the `kIOHIDProductKey` in native C.
   */
  String productName;

  /**
   * The manufacturer name of the HID device.
   * Corresponds to the `kIOHIDManufacturerKey` in native C.
   */
  String manufacturer;

  /**
   * The transport type of the HID device (e.g., USB, Bluetooth).
   * Corresponds to the `kIOHIDTransportKey` in native C.
   */
  String transport;

  /**
   * The usage ID of the HID device, which defines the device's primary usage.
   * Corresponds to the `kIOHIDPrimaryUsageKey` in native C.
   */
  int usage;

  /**
   * The usage page of the HID device, which groups related usages together.
   * Corresponds to the `kIOHIDPrimaryUsagePageKey` in native C.
   */
  int usagePage;


  InputDevice inputDevice;

  /**
   * Gets an unmodifiable list of elements associated with this HID device.
   *
   * @return the list of elements.
   */
  List<IOHIDElement> getElements() {
    return Collections.unmodifiableList(elements);
  }


  /**
   * Adds an element to this HID device.
   *
   * @param element the element to add.
   */
  void addElement(IOHIDElement element) {
    elements.add(element);
  }

  /**
   * Checks if this HID device is a supported device type (e.g., gamepad, joystick).
   *
   * @return true if the device is supported, false otherwise.
   */
  boolean isSupportedHIDDevice() {
    return IOHIDDeviceType.GAMEPAD.isType(this.usagePage, this.usage) || IOHIDDeviceType.JOYSTICK.isType(this.usagePage, this.usage);
  }

  @Override
  public String toString() {
    return ", product: '" + productName + "' (" + String.format("0X%02X", productId) + ")" +
            ", vendor: '" + manufacturer + "' (" + String.format("0X%02X", vendorId) + ")" +
            ", transport: '" + transport + '\'' +
            ", usage: " + String.format("0X%02X", usage) + " (page: " + String.format("0X%02X", usagePage) + ")";
  }

  /**
   * Enum representing the different types of IOHID devices.
   *
   * <p>This enum is limited to the types of devices that are supported by this library.
   * There are many other types of devices that can be represented by an IOHIDDevice.</p>
   */
  private enum IOHIDDeviceType {
    KEYBOARD(0x01, 0x06),
    MOUSE(0x01, 0x02),
    GAMEPAD(0x01, 0x05),
    JOYSTICK(0x01, 0x04);

    private final int primaryUsagePage;
    private final int primaryUsage;

    IOHIDDeviceType(int primaryUsagePage, int primaryUsage) {
      this.primaryUsagePage = primaryUsagePage;
      this.primaryUsage = primaryUsage;
    }

    public int getPrimaryUsagePage() {
      return primaryUsagePage;
    }

    public int getPrimaryUsage() {
      return primaryUsage;
    }

    public boolean isType(int usagePage, int usage) {
      return this.primaryUsagePage == usagePage && this.primaryUsage == usage;
    }
  }
}
