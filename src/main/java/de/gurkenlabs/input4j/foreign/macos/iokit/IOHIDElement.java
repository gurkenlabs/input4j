package de.gurkenlabs.input4j.foreign.macos.iokit;

import de.gurkenlabs.input4j.ComponentType;
import de.gurkenlabs.input4j.InputComponent;

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
   * Returns the component type of the HID element.
   *
   * @return the component type of the HID element
   */
  public ComponentType getComponentType() {
    return switch (this.getUsage()) {
      case BUTTON_1, BUTTON_2, BUTTON_3, BUTTON_4, BUTTON_5, BUTTON_6, BUTTON_7, BUTTON_8, BUTTON_9, BUTTON_10,
           BUTTON_11, BUTTON_12, BUTTON_13, BUTTON_14, BUTTON_15, BUTTON_16, BUTTON_17, BUTTON_18, BUTTON_19, BUTTON_20,
           BUTTON_21, BUTTON_22, BUTTON_23, BUTTON_24, BUTTON_25, BUTTON_26, BUTTON_27, BUTTON_28, BUTTON_29, BUTTON_30,
           BUTTON_31, BUTTON_32 -> ComponentType.Button;
      case X, Y, Z, RX, RY, RZ, SLIDER, DIAL, WHEEL, HAT_SWITCH -> ComponentType.Axis;
      default -> ComponentType.Unknown;
    };
  }

  /**
   * Returns the identifier of the HID element.
   *
   * @return the identifier of the HID element
   */
  public InputComponent.ID getIdentifier() {
    return switch (this.getUsage()) {
      case BUTTON_1 -> InputComponent.BUTTON_1;
      case BUTTON_2 -> InputComponent.BUTTON_2;
      case BUTTON_3 -> InputComponent.BUTTON_3;
      case BUTTON_4 -> InputComponent.BUTTON_4;
      case BUTTON_5 -> InputComponent.BUTTON_5;
      case BUTTON_6 -> InputComponent.BUTTON_6;
      case BUTTON_7 -> InputComponent.BUTTON_7;
      case BUTTON_8 -> InputComponent.BUTTON_8;
      case BUTTON_9 -> InputComponent.BUTTON_9;
      case BUTTON_10 -> InputComponent.BUTTON_10;
      case BUTTON_11 -> InputComponent.BUTTON_11;
      case BUTTON_12 -> InputComponent.BUTTON_12;
      case BUTTON_13 -> InputComponent.BUTTON_13;
      case BUTTON_14 -> InputComponent.BUTTON_14;
      case BUTTON_15 -> InputComponent.BUTTON_15;
      case BUTTON_16 -> InputComponent.BUTTON_16;
      case BUTTON_17 -> InputComponent.BUTTON_17;
      case BUTTON_18 -> InputComponent.BUTTON_18;
      case BUTTON_19 -> InputComponent.BUTTON_19;
      case BUTTON_20 -> InputComponent.BUTTON_20;
      case BUTTON_21 -> InputComponent.BUTTON_21;
      case BUTTON_22 -> InputComponent.BUTTON_22;
      case BUTTON_23 -> InputComponent.BUTTON_23;
      case BUTTON_24 -> InputComponent.BUTTON_24;
      case BUTTON_25 -> InputComponent.BUTTON_25;
      case BUTTON_26 -> InputComponent.BUTTON_26;
      case BUTTON_27 -> InputComponent.BUTTON_27;
      case BUTTON_28 -> InputComponent.BUTTON_28;
      case BUTTON_29 -> InputComponent.BUTTON_29;
      case BUTTON_30 -> InputComponent.BUTTON_30;
      case BUTTON_31 -> InputComponent.BUTTON_31;
      case X -> InputComponent.AXIS_X;
      case Y -> InputComponent.AXIS_Y;
      case Z -> InputComponent.AXIS_Z;
      case RX -> InputComponent.AXIS_RX;
      case RY -> InputComponent.AXIS_RY;
      case RZ -> InputComponent.AXIS_RZ;
      case SLIDER -> InputComponent.AXIS_SLIDER;
      case HAT_SWITCH -> InputComponent.AXIS_DPAD;
      default -> new InputComponent.ID(ComponentType.Unknown, InputComponent.ID.getNextId(), this.usage.name());
    };
  }

}
