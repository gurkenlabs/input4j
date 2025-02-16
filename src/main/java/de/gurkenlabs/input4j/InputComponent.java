package de.gurkenlabs.input4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code InputComponent} class represents a component of an input device, such as a button or axis.
 * It holds information about the component's type, name, and whether it provides relative input.
 * The input data for the component is set by the associated {@link InputDevice} when polling.
 */
public final class InputComponent {
  public static final ID BUTTON_0 = new ID(ComponentType.Button, 0, "BUTTON_0", 0);
  public static final ID BUTTON_1 = new ID(ComponentType.Button, BUTTON_0.id + 1, "BUTTON_1", 0);
  public static final ID BUTTON_2 = new ID(ComponentType.Button, BUTTON_0.id + 2, "BUTTON_2", 0);
  public static final ID BUTTON_3 = new ID(ComponentType.Button, BUTTON_0.id + 3, "BUTTON_3", 0);
  public static final ID BUTTON_4 = new ID(ComponentType.Button, BUTTON_0.id + 4, "BUTTON_4", 0);
  public static final ID BUTTON_5 = new ID(ComponentType.Button, BUTTON_0.id + 5, "BUTTON_5", 0);
  public static final ID BUTTON_6 = new ID(ComponentType.Button, BUTTON_0.id + 6, "BUTTON_6", 0);
  public static final ID BUTTON_7 = new ID(ComponentType.Button, BUTTON_0.id + 7, "BUTTON_7", 0);
  public static final ID BUTTON_8 = new ID(ComponentType.Button, BUTTON_0.id + 8, "BUTTON_8", 0);
  public static final ID BUTTON_9 = new ID(ComponentType.Button, BUTTON_0.id + 9, "BUTTON_9", 0);
  public static final ID BUTTON_10 = new ID(ComponentType.Button, BUTTON_0.id + 10, "BUTTON_10", 0);
  public static final ID BUTTON_11 = new ID(ComponentType.Button, BUTTON_0.id + 11, "BUTTON_11", 0);
  public static final ID BUTTON_12 = new ID(ComponentType.Button, BUTTON_0.id + 12, "BUTTON_12", 0);
  public static final ID BUTTON_13 = new ID(ComponentType.Button, BUTTON_0.id + 13, "BUTTON_13", 0);
  public static final ID BUTTON_14 = new ID(ComponentType.Button, BUTTON_0.id + 14, "BUTTON_14", 0);
  public static final ID BUTTON_15 = new ID(ComponentType.Button, BUTTON_0.id + 15, "BUTTON_15", 0);
  public static final ID BUTTON_16 = new ID(ComponentType.Button, BUTTON_0.id + 16, "BUTTON_16", 0);
  public static final ID BUTTON_17 = new ID(ComponentType.Button, BUTTON_0.id + 17, "BUTTON_17", 0);
  public static final ID BUTTON_18 = new ID(ComponentType.Button, BUTTON_0.id + 18, "BUTTON_18", 0);
  public static final ID BUTTON_19 = new ID(ComponentType.Button, BUTTON_0.id + 19, "BUTTON_19", 0);
  public static final ID BUTTON_20 = new ID(ComponentType.Button, BUTTON_0.id + 20, "BUTTON_20", 0);
  public static final ID BUTTON_21 = new ID(ComponentType.Button, BUTTON_0.id + 21, "BUTTON_21", 0);
  public static final ID BUTTON_22 = new ID(ComponentType.Button, BUTTON_0.id + 22, "BUTTON_22", 0);
  public static final ID BUTTON_23 = new ID(ComponentType.Button, BUTTON_0.id + 23, "BUTTON_23", 0);
  public static final ID BUTTON_24 = new ID(ComponentType.Button, BUTTON_0.id + 24, "BUTTON_24", 0);
  public static final ID BUTTON_25 = new ID(ComponentType.Button, BUTTON_0.id + 25, "BUTTON_25", 0);
  public static final ID BUTTON_26 = new ID(ComponentType.Button, BUTTON_0.id + 26, "BUTTON_26", 0);
  public static final ID BUTTON_27 = new ID(ComponentType.Button, BUTTON_0.id + 27, "BUTTON_27", 0);
  public static final ID BUTTON_28 = new ID(ComponentType.Button, BUTTON_0.id + 28, "BUTTON_28", 0);
  public static final ID BUTTON_29 = new ID(ComponentType.Button, BUTTON_0.id + 29, "BUTTON_29", 0);
  public static final ID BUTTON_30 = new ID(ComponentType.Button, BUTTON_0.id + 30, "BUTTON_30", 0);
  public static final ID BUTTON_31 = new ID(ComponentType.Button, BUTTON_0.id + 31, "BUTTON_31", 0);
  public static final ID DPAD_UP = new ID(ComponentType.Button, BUTTON_31.id + 1, "DPAD_UP", 0);
  public static final ID DPAD_RIGHT = new ID(ComponentType.Button, DPAD_UP.id + 1, "DPAD_RIGHT", 0);
  public static final ID DPAD_DOWN = new ID(ComponentType.Button, DPAD_UP.id + 2, "DPAD_DOWN", 0);
  public static final ID DPAD_LEFT = new ID(ComponentType.Button, DPAD_UP.id + 3, "DPAD_LEFT", 0);
  public static final ID AXIS_X = new ID(ComponentType.Axis, DPAD_LEFT.id + 1, "LEFT_AXIS_X", 0);
  public static final ID AXIS_Y = new ID(ComponentType.Axis, AXIS_X.id + 1, "LEFT_AXIS_Y", 0);
  public static final ID AXIS_Z = new ID(ComponentType.Axis, AXIS_X.id + 2, "LEFT_AXIS_Z", 0);
  public static final ID AXIS_RX = new ID(ComponentType.Axis, AXIS_X.id + 3, "RIGHT_AXIS_X", 0);
  public static final ID AXIS_RY = new ID(ComponentType.Axis, AXIS_X.id + 4, "RIGHT_AXIS_Y", 0);
  public static final ID AXIS_RZ = new ID(ComponentType.Axis, AXIS_X.id + 5, "RIGHT_AXIS_Z", 0);
  public static final ID AXIS_SLIDER = new ID(ComponentType.Axis, AXIS_X.id + 6, "SLIDER", 0);
  public static final ID AXIS_DPAD = new ID(ComponentType.Axis, AXIS_X.id + 7, "DPAD_AXIS", 0);

  private final InputDevice device;
  private final ID id;

  private final String originalName;
  private final boolean relative;

  private float data;

  public InputComponent(InputDevice device, ID id) {
    this(device, id, false);
  }

  /**
   * Creates a new instance of the InputComponent class.
   *
   * @param device   the input device associated with this component
   * @param relative true if the component provides relative input, false otherwise
   */
  public InputComponent(InputDevice device, ID id, boolean relative) {
    this(device, id, null, relative);
  }


  public InputComponent(InputDevice device, ID id, String originalName) {
    this(device, id, originalName, false);

  }

  /**
   * Creates a new instance of the InputComponent class.
   *
   * @param device       the input device associated with this component
   * @param originalName the name of the component
   * @param relative     true if the component provides relative input, false otherwise
   */
  public InputComponent(InputDevice device, ID id, String originalName, boolean relative) {
    this.device = device;
    this.id = id;
    this.originalName = originalName;
    this.relative = relative;
  }

  /**
   * Gets the input data for a specific input component.
   * The data is set by the {@link InputDevice} when polling.
   *
   * @return the input data for the specified component
   */
  public float getData() {
    return this.data;
  }

  /**
   * Gets the type of this component.
   *
   * @return the type of this component
   */
  public ComponentType getType() {
    return this.getId().type;
  }

  public ID getId() {
    return id;
  }

  /**
   * Gets the name of this component.
   *
   * @return the name of this component
   */
  public String getOriginalName() {
    return originalName;
  }

  /**
   * Checks if this component is an axis.
   *
   * @return true if this component is an axis, false otherwise
   */
  public boolean isAxis() {
    return id.type.isAxis();
  }

  /**
   * Checks if this component provides relative input.
   *
   * @return true if this component provides relative input, false otherwise
   */
  public boolean isRelative() {
    return relative;
  }

  /**
   * Gets the input device associated with this component.
   *
   * @return the input device associated with this component
   */
  public InputDevice getDevice() {
    return device;
  }

  /**
   * Sets the input data for this component.
   *
   * @param data the input data to set
   */
  public void setData(float data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return this.getId().name + ": " + this.getData();
  }

  /**
   * The {@code ID} class represents a unique identifier for input components.
   * It is used to manage and reference different input components, such as buttons, axes, and other controls.
   * The class ensures that each {@code ID} is unique and provides functionality to remap IDs for better accessibility.
   *
   * <p>Note: The {@code int} value needs to be unique. The {@link #getNextId()} method can be used to ensure this.</p>
   *
   * <p>Example usage:</p>
   * <pre>{@code
   * // Creating a new ID
   * InputComponent.ID customID = new InputComponent.ID(100, "CUSTOM_BUTTON");
   *
   * // Remapping an existing ID
   * InputComponent.ID remappedID = new InputComponent.ID(InputComponent.Button.get(1), "REMAPPED_BUTTON");
   * }</pre>
   *
   * <p>Remapping IDs for Controllers (see the predefined examples for XInput and DualShock4):</p>
   *
   * <p>XInput:</p>
   * <pre>{@code
   * public static final XInput A = new XInput(InputComponent.Button.get(0), "A");
   * }</pre>
   *
   * <p>DualShock4:</p>
   * <pre>{@code
   * public static final DualShock4 SQUARE = new DualShock4(InputComponent.Button.get(0), "SQUARE");
   * }</pre>
   */
  public static class ID {
    public final int id;
    public final int nativeId;
    public final ComponentType type;
    public String name;

    private static final List<ID> ids = new CopyOnWriteArrayList<>();

    /**
     * If you want to make this ID accessible with a different field, you can use this constructor.
     *
     * @param otherId the ID to remap
     */
    public ID(ID otherId) {
      this(otherId.type, otherId.id, otherId.name, 0);
    }

    public ID(ID otherId, int nativeId) {
      this(otherId.type, otherId.id, otherId.name, nativeId);
    }

    public ID(ID otherId, String name) {
      this(otherId.type, otherId.id, name, 0);
    }

    /**
     * If you want to make this ID available with a different name, you can use this constructor.
     *
     * @param otherId the ID to remap
     * @param name    the name of the new ID
     */
    public ID(ID otherId, String name, int nativeId) {
      this(otherId.type, otherId.id, name, nativeId);
    }

    public ID(ComponentType type, int id, String name) {
      this(type, id, name, 0);
    }

    public ID(ComponentType type, int id, String name, int nativeId) {
      this.type = type;
      this.id = id;
      this.name = name;
      this.nativeId = nativeId;
      // exclude remapped IDs, only add the original ID
      if (ids.stream().noneMatch(i -> i.id == this.id)) {
        ids.add(this);
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ID identifier) {
        return identifier.type == this.type && identifier.id == this.id;
      }
      return false;
    }

    /**
     * Gets the next unique ID value.
     *
     * @return the next unique ID value
     */
    public static int getNextId() {
      return ids.stream().mapToInt(id -> id.id).max().orElse(0) + 1;
    }

    public static ID get(int id) {
      return ids.stream().filter(i -> i.id == id).findFirst().orElse(null);
    }

    public static ID getButton(int id) {
      return ids.stream().filter(i -> i.type == ComponentType.Button && i.id == id).findFirst().orElse(null);
    }

    public static ID get(String name) {
      return ids.stream().filter(i -> i.name.equals(name)).findFirst().orElse(null);
    }
  }

  public static class XInput extends ID {
    public static final XInput A = new XInput(InputComponent.BUTTON_0, "A");
    public static final XInput B = new XInput(InputComponent.BUTTON_1, "B");
    public static final XInput X = new XInput(InputComponent.BUTTON_2, "X");
    public static final XInput Y = new XInput(InputComponent.BUTTON_3, "Y");
    public static final XInput LEFT_SHOULDER = new XInput(InputComponent.BUTTON_4, "LEFT_SHOULDER");
    public static final XInput RIGHT_SHOULDER = new XInput(InputComponent.BUTTON_5, "RIGHT_SHOULDER");
    public static final XInput BACK = new XInput(InputComponent.BUTTON_6, "BACK");
    public static final XInput START = new XInput(InputComponent.BUTTON_7, "START");
    public static final XInput LEFT_THUMB = new XInput(InputComponent.BUTTON_8, "LEFT_THUMB");
    public static final XInput RIGHT_THUMB = new XInput(InputComponent.BUTTON_9, "RIGHT_THUMB");
    public static final XInput DPAD_UP = new XInput(InputComponent.DPAD_UP);
    public static final XInput DPAD_DOWN = new XInput(InputComponent.DPAD_DOWN);
    public static final XInput DPAD_LEFT = new XInput(InputComponent.DPAD_LEFT);
    public static final XInput DPAD_RIGHT = new XInput(InputComponent.DPAD_RIGHT);
    public static final XInput LEFT_THUMB_X = new XInput(InputComponent.AXIS_X, "LEFT_THUMB_X");
    public static final XInput LEFT_THUMB_Y = new XInput(InputComponent.AXIS_Y, "LEFT_THUMB_Y");
    public static final XInput RIGHT_THUMB_X = new XInput(InputComponent.AXIS_RX, "RIGHT_THUMB_X");
    public static final XInput RIGHT_THUMB_Y = new XInput(InputComponent.AXIS_RY, "RIGHT_THUMB_Y");
    public static final XInput LEFT_TRIGGER = new XInput(InputComponent.AXIS_Z, "LEFT_TRIGGER");
    public static final XInput RIGHT_TRIGGER = new XInput(InputComponent.AXIS_RZ, "RIGHT_TRIGGER");
    public static final XInput DPAD = new XInput(InputComponent.AXIS_DPAD);

    private XInput(ID otherId, String name) {
      super(otherId, name);
    }

    private XInput(ID otherId) {
      super(otherId);
    }
  }

  public static class DualShock4 extends ID {
    public static final DualShock4 SQUARE = new DualShock4(InputComponent.BUTTON_0, "SQUARE");
    public static final DualShock4 CROSS = new DualShock4(InputComponent.BUTTON_1, "CROSS");
    public static final DualShock4 CIRCLE = new DualShock4(InputComponent.BUTTON_2, "CIRCLE");
    public static final DualShock4 TRIANGLE = new DualShock4(InputComponent.BUTTON_3, "TRIANGLE");
    public static final DualShock4 L1 = new DualShock4(InputComponent.BUTTON_4, "L1");
    public static final DualShock4 R1 = new DualShock4(InputComponent.BUTTON_5, "R1");
    public static final DualShock4 L2 = new DualShock4(InputComponent.BUTTON_6, "L2");
    public static final DualShock4 R2 = new DualShock4(InputComponent.BUTTON_7, "R2");
    public static final DualShock4 SHARE = new DualShock4(InputComponent.BUTTON_8, "SHARE");
    public static final DualShock4 OPTIONS = new DualShock4(InputComponent.BUTTON_9, "OPTIONS");
    public static final DualShock4 LEFT_THUMB_PRESS = new DualShock4(InputComponent.BUTTON_10, "LEFT_THUMB_PRESS");
    public static final DualShock4 RIGHT_THUMB_PRESS = new DualShock4(InputComponent.BUTTON_11, "RIGHT_THUMB_PRESS");
    public static final DualShock4 PS = new DualShock4(InputComponent.BUTTON_12, "PS");
    public static final DualShock4 TOUCHPAD = new DualShock4(InputComponent.BUTTON_13, "TOUCHPAD");
    public static final DualShock4 DPAD_UP = new DualShock4(InputComponent.DPAD_UP);
    public static final DualShock4 DPAD_DOWN = new DualShock4(InputComponent.DPAD_DOWN);
    public static final DualShock4 DPAD_LEFT = new DualShock4(InputComponent.DPAD_LEFT);
    public static final DualShock4 DPAD_RIGHT = new DualShock4(InputComponent.DPAD_RIGHT);
    public static final DualShock4 DPAD = new DualShock4(InputComponent.AXIS_DPAD);
    public static final DualShock4 LEFT_THUMB_X = new DualShock4(InputComponent.AXIS_X, "LEFT_THUMB_X");
    public static final DualShock4 LEFT_THUMB_Y = new DualShock4(InputComponent.AXIS_Y, "LEFT_THUMB_Y");
    public static final DualShock4 RIGHT_THUMB_X = new DualShock4(InputComponent.AXIS_Z, "RIGHT_THUMB_X"); // this is actually the Z axis on the DS4
    public static final DualShock4 RIGHT_THUMB_Y = new DualShock4(InputComponent.AXIS_RZ, "RIGHT_THUMB_Y"); // this is actually the RZ axis on the DS4
    public static final DualShock4 LEFT_TRIGGER = new DualShock4(InputComponent.AXIS_RX, "LEFT_TRIGGER");
    public static final DualShock4 RIGHT_TRIGGER = new DualShock4(InputComponent.AXIS_RY, "RIGHT_TRIGGER");

    private DualShock4(ID otherId, String name) {
      super(otherId, name);
    }

    private DualShock4(ID otherId) {
      super(otherId);
    }
  }
}
