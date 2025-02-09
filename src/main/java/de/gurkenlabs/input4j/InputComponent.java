package de.gurkenlabs.input4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code InputComponent} class represents a component of an input device, such as a button or axis.
 * It holds information about the component's type, name, and whether it provides relative input.
 * The input data for the component is set by the associated {@link InputDevice} when polling.
 */
public final class InputComponent {
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
    public String name;
    public ComponentType type;

    private static final List<ID> ids = new CopyOnWriteArrayList<>();

    /**
     * If you want to make this ID accessible with a different field, you can use this constructor.
     *
     * @param otherId the ID to remap
     */
    public ID(ID otherId) {
      this(otherId.type, otherId.id, otherId.name);
    }

    /**
     * If you want to make this ID available with a different name, you can use this constructor.
     *
     * @param otherId the ID to remap
     * @param name    the name of the new ID
     */
    public ID(ID otherId, String name) {
      this(otherId.type, otherId.id, name);
    }

    public ID(ComponentType type, int id, String name) {
      this.type = type;
      this.id = id;
      this.name = name;

      // exclude remapped IDs, only add the original ID
      if (ids.stream().noneMatch(i -> i.id == this.id)) {
        ids.add(this);
      }
    }

    /**
     * Gets the next unique ID value.
     *
     * @return the next unique ID value
     */
    public static int getNextId() {
      return ids.size() + 1;
    }

    public static ID get(int id) {
      return ids.stream().filter(i -> i.id == id).findFirst().orElse(null);
    }

    public static ID get(String name) {
      return ids.stream().filter(i -> i.name.equals(name)).findFirst().orElse(null);
    }
  }

  public static class Button extends ID {
    public static final int MAX_BUTTON = 32;
    private static final List<Button> buttons = new CopyOnWriteArrayList<>();

    static {
      for (int i = 0; i < MAX_BUTTON; i++) {
        buttons.add(new Button(i, "BUTTON_" + i));
      }
    }

    public static Button get(int id) {
      return buttons.stream().filter(b -> b.id == id).findFirst().orElse(null);
    }

    public Button(int id, String name) {
      super(ComponentType.Button, id, name);
    }
  }

  public static class Dpad extends ID {
    public static final int MAX_DPAD = Button.MAX_BUTTON + 4;

    public static final Dpad UP = new Dpad(Button.MAX_BUTTON + 1, "DPAD_UP");
    public static final Dpad RIGHT = new Dpad(Dpad.UP.id + 1, "DPAD_RIGHT");
    public static final Dpad DOWN = new Dpad(Dpad.UP.id + 2, "DPAD_DOWN");
    public static final Dpad LEFT = new Dpad(Dpad.UP.id + 3, "DPAD_LEFT");

    public Dpad(int id, String name) {
      super(ComponentType.Button, id, name);
    }
  }

  public static class Axis extends ID {
    public static final Axis X = new Axis(Dpad.MAX_DPAD + 1, "LEFT_AXIS_X");
    public static final Axis Y = new Axis(Axis.X.id + 1, "LEFT_AXIS_Y");
    public static final Axis Z = new Axis(Axis.X.id + 2, "LEFT_AXIS_Z");
    public static final Axis RX = new Axis(Axis.X.id + 3, "RIGHT_AXIS_X");
    public static final Axis RY = new Axis(Axis.X.id + 4, "RIGHT_AXIS_Y");
    public static final Axis RZ = new Axis(Axis.X.id + 5, "RIGHT_AXIS_Z");
    public static final Axis SLIDER = new Axis(Axis.X.id + 6, "SLIDER");
    public static final Axis DPAD = new Axis(ComponentType.DPad, Axis.X.id + 7, "DPAD_AXIS");

    public Axis(int id, String name) {
      super(ComponentType.Axis, id, name);
    }

    public Axis(ComponentType type, int id, String name) {
      super(type, id, name);
    }
  }

  public static class XInput extends ID {
    public static final XInput A = new XInput(Button.get(0), "A");
    public static final XInput B = new XInput(Button.get(1), "B");
    public static final XInput X = new XInput(Button.get(2), "X");
    public static final XInput Y = new XInput(Button.get(3), "Y");
    public static final XInput LEFT_SHOULDER = new XInput(Button.get(4), "LEFT_SHOULDER");
    public static final XInput RIGHT_SHOULDER = new XInput(Button.get(5), "RIGHT_SHOULDER");
    public static final XInput BACK = new XInput(Button.get(6), "BACK");
    public static final XInput START = new XInput(Button.get(7), "START");
    public static final XInput LEFT_THUMB = new XInput(Button.get(8), "LEFT_THUMB");
    public static final XInput RIGHT_THUMB = new XInput(Button.get(9), "RIGHT_THUMB");
    public static final XInput DPAD_UP = new XInput(Dpad.UP);
    public static final XInput DPAD_DOWN = new XInput(Dpad.DOWN);
    public static final XInput DPAD_LEFT = new XInput(Dpad.LEFT);
    public static final XInput DPAD_RIGHT = new XInput(Dpad.RIGHT);
    public static final XInput LEFT_THUMB_X = new XInput(Axis.X, "LEFT_THUMB_X");
    public static final XInput LEFT_THUMB_Y = new XInput(Axis.Y, "LEFT_THUMB_Y");
    public static final XInput RIGHT_THUMB_X = new XInput(Axis.RX, "RIGHT_THUMB_X");
    public static final XInput RIGHT_THUMB_Y = new XInput(Axis.RY, "RIGHT_THUMB_Y");
    public static final XInput LEFT_TRIGGER = new XInput(Axis.Z, "LEFT_TRIGGER");
    public static final XInput RIGHT_TRIGGER = new XInput(Axis.RZ, "RIGHT_TRIGGER");
    public static final XInput DPAD = new XInput(Axis.DPAD);

    private XInput(ID otherId, String name) {
      super(otherId, name);
    }

    private XInput(ID otherId) {
      super(otherId);
    }
  }

  public static class DualShock4 extends ID {
    public static final DualShock4 SQUARE = new DualShock4(Button.get(0), "SQUARE");
    public static final DualShock4 CROSS = new DualShock4(Button.get(1), "CROSS");
    public static final DualShock4 CIRCLE = new DualShock4(Button.get(2), "CIRCLE");
    public static final DualShock4 TRIANGLE = new DualShock4(Button.get(3), "TRIANGLE");
    public static final DualShock4 L1 = new DualShock4(Button.get(4), "L1");
    public static final DualShock4 R1 = new DualShock4(Button.get(5), "R1");
    public static final DualShock4 L2 = new DualShock4(Button.get(6), "L2");
    public static final DualShock4 R2 = new DualShock4(Button.get(7), "R2");
    public static final DualShock4 SHARE = new DualShock4(Button.get(8), "SHARE");
    public static final DualShock4 OPTIONS = new DualShock4(Button.get(9), "OPTIONS");
    public static final DualShock4 LEFT_THUMB_PRESS = new DualShock4(Button.get(10), "LEFT_THUMB_PRESS");
    public static final DualShock4 RIGHT_THUMB_PRESS = new DualShock4(Button.get(10), "RIGHT_THUMB_PRESS");
    public static final DualShock4 PS = new DualShock4(Button.get(12), "PS");
    public static final DualShock4 TOUCHPAD = new DualShock4(Button.get(13), "TOUCHPAD");
    public static final DualShock4 DPAD_UP = new DualShock4(Dpad.UP);
    public static final DualShock4 DPAD_DOWN = new DualShock4(Dpad.DOWN);
    public static final DualShock4 DPAD_LEFT = new DualShock4(Dpad.LEFT);
    public static final DualShock4 DPAD_RIGHT = new DualShock4(Dpad.RIGHT);
    public static final DualShock4 DPAD = new DualShock4(Axis.DPAD);
    public static final DualShock4 LEFT_THUMB_X = new DualShock4(Axis.X, "LEFT_THUMB_X");
    public static final DualShock4 LEFT_THUMB_Y = new DualShock4(Axis.Y, "LEFT_THUMB_Y");
    public static final DualShock4 RIGHT_THUMB_X = new DualShock4(Axis.Z, "RIGHT_THUMB_X"); // this is actually the Z axis on the DS4
    public static final DualShock4 RIGHT_THUMB_Y = new DualShock4(Axis.RZ, "RIGHT_THUMB_Y"); // this is actually the RZ axis on the DS4
    public static final DualShock4 LEFT_TRIGGER = new DualShock4(Axis.RX, "LEFT_TRIGGER");
    public static final DualShock4 RIGHT_TRIGGER = new DualShock4(Axis.RY, "RIGHT_TRIGGER");

    private DualShock4(ID otherId, String name) {
      super(otherId, name);
    }

    private DualShock4(ID otherId) {
      super(otherId);
    }
  }
}
