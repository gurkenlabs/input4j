package de.gurkenlabs.input4j.foreign.macos.iokit;

import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.components.Axis;
import de.gurkenlabs.input4j.components.Button;

/**
 * Represents an IOHIDElement, which is a control element of an IOHIDDevice on macOS.
 * This class encapsulates the properties and values of an HID element.
 */
class IOHIDElement {
  /**
   * The memory address of the HID element.
   * Corresponds to the `IOHIDElementRef` in native C.
   */
  long address;

  /**
   * The name of the HID element.
   * Corresponds to the `kIOHIDElementNameKey` in native C.
   */
  String name;

  /**
   * The type of the HID element.
   * Corresponds to the `kIOHIDElementTypeKey` in native C.
   */
  IOHIDElementType type;

  /**
   * The usage of the HID element, which defines the element's specific function.
   * Corresponds to the `kIOHIDElementUsageKey` in native C.
   */
  IOHIDElementUsage usage;

  /**
   * The usage page of the HID element, which groups related usages together.
   * Corresponds to the `kIOHIDElementUsagePageKey` in native C.
   */
  IOHIDElementUsagePage usagePage;

  /**
   * The minimum value that the HID element can report.
   * Corresponds to the `kIOHIDElementMinKey` in native C.
   */
  int min;

  /**
   * The maximum value that the HID element can report.
   * Corresponds to the `kIOHIDElementMaxKey` in native C.
   */
  int max;

  /**
   * The physical minimum value that the HID element can report.
   * Corresponds to the `kIOHIDElementPhysicalMinKey` in native C.
   */
  int physicalMin;

  /**
   * The physical maximum value that the HID element can report.
   * Corresponds to the `kIOHIDElementPhysicalMaxKey` in native C.
   */
  int physicalMax;

  /**
   * The unit of measurement for the HID element's value.
   * Corresponds to the `kIOHIDElementUnitKey` in native C.
   */
  int unit;

  /**
   * The unit exponent for the HID element's value.
   * Corresponds to the `kIOHIDElementUnitExponentKey` in native C.
   */
  int unitExponent;

  /**
   * The size of the report for the HID element.
   * Corresponds to the `kIOHIDElementReportSizeKey` in native C.
   */
  int reportSize;

  int currentValue;

  String getName() {
    if (this.name != null) {
      return this.name;
    }

    if (this.getUsage() != null && this.getUsage() != IOHIDElementUsage.UNDEFINED) {
      return this.getUsage().toString();
    }

    return "";
  }

  /**
   * Returns the usage of the HID element.
   * If the element is not a button and the usage is less than 32, returns `IOHIDElementUsage.UNDEFINED`.
   *
   * @return the usage of the HID element
   */
  IOHIDElementUsage getUsage() {
    if (this.type != IOHIDElementType.BUTTON && this.usage.getUsage() < IOHIDElementUsage.BUTTON_32.getUsage()) {
      return IOHIDElementUsage.UNDEFINED;
    }
    return this.usage;
  }

  @Override
  public String toString() {
    return " name: '" + getName() + "'" +
            ", usage: " + getUsage() +
            ", type: " + type + " (" + usagePage + ")" +
            ", min: '" + min +
            ", max: '" + max;
  }

  /**
   * Returns the identifier of the HID element.
   *
   * @return the identifier of the HID element
   */
  public InputComponent.ID getIdentifier() {
    return switch (this.getUsage()) {
      case BUTTON_1 -> Button.BUTTON_0;
      case BUTTON_2 -> Button.BUTTON_1;
      case BUTTON_3 -> Button.BUTTON_2;
      case BUTTON_4 -> Button.BUTTON_3;
      case BUTTON_5 -> Button.BUTTON_4;
      case BUTTON_6 -> Button.BUTTON_5;
      case BUTTON_7 -> Button.BUTTON_6;
      case BUTTON_8 -> Button.BUTTON_7;
      case BUTTON_9 -> Button.BUTTON_8;
      case BUTTON_10 -> Button.BUTTON_9;
      case BUTTON_11 -> Button.BUTTON_10;
      case BUTTON_12 -> Button.BUTTON_11;
      case BUTTON_13 -> Button.BUTTON_12;
      case BUTTON_14 -> Button.BUTTON_13;
      case BUTTON_15 -> Button.BUTTON_14;
      case BUTTON_16 -> Button.BUTTON_15;
      case BUTTON_17 -> Button.BUTTON_16;
      case BUTTON_18 -> Button.BUTTON_17;
      case BUTTON_19 -> Button.BUTTON_18;
      case BUTTON_20 -> Button.BUTTON_19;
      case BUTTON_21 -> Button.BUTTON_20;
      case BUTTON_22 -> Button.BUTTON_21;
      case BUTTON_23 -> Button.BUTTON_22;
      case BUTTON_24 -> Button.BUTTON_23;
      case BUTTON_25 -> Button.BUTTON_24;
      case BUTTON_26 -> Button.BUTTON_25;
      case BUTTON_27 -> Button.BUTTON_26;
      case BUTTON_28 -> Button.BUTTON_27;
      case BUTTON_29 -> Button.BUTTON_28;
      case BUTTON_30 -> Button.BUTTON_29;
      case BUTTON_31 -> Button.BUTTON_30;
      case BUTTON_32 -> Button.BUTTON_31;
      case X -> Axis.AXIS_X;
      case Y -> Axis.AXIS_Y;
      case Z -> Axis.AXIS_Z;
      case RX -> Axis.AXIS_RX;
      case RY -> Axis.AXIS_RY;
      case RZ -> Axis.AXIS_RZ;
      case SLIDER -> Axis.AXIS_SLIDER;
      case HAT_SWITCH -> Axis.AXIS_DPAD;
      default -> new InputComponent.ID(ComponentType.UNKNOWN, InputComponent.ID.getNextId(ComponentType.UNKNOWN, 0), this.usage.name());
    };
  }

}
