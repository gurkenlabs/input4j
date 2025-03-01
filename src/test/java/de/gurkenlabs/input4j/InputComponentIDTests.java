package de.gurkenlabs.input4j;

import de.gurkenlabs.input4j.components.Axis;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InputComponentIDTests {

  @Test
  public void testIDCreation() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    assertEquals(ComponentType.BUTTON, id.type);
    assertEquals(1, id.id);
    assertEquals("BUTTON_1", id.name);
  }

  @Test
  public void testIDEquality() {
    InputComponent.ID id1 = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent.ID id2 = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent.ID id3 = new InputComponent.ID(id2, "REMAPPED_BUTTON");
    assertEquals(id1, id2);
    assertEquals(id2, id3);
    assertEquals(id2.hashCode(), id3.hashCode());
  }

  @Test
  public void testIDInequality() {
    InputComponent.ID id1 = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent.ID id2 = new InputComponent.ID(ComponentType.AXIS, 1, "AXIS_1");
    assertNotEquals(id1, id2);
  }

  @Test
  public void testRemappedID() {
    InputComponent.ID original = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    InputComponent.ID remapped = new InputComponent.ID(original, "REMAPPED_BUTTON");
    assertEquals(original.type, remapped.type);
    assertEquals(original.id, remapped.id);
    assertEquals("REMAPPED_BUTTON", remapped.name);
  }

  @Test
  public void testGetNextId() {
    int nextId = InputComponent.ID.getNextButtonId();
    InputComponent.ID newId = new InputComponent.ID(ComponentType.BUTTON, nextId, "NEW_BUTTON");
    assertEquals(nextId, newId.id);
  }

  @Test
  public void testNextIdLeavesRoomForDefaultComponents() {
    int nextId = InputComponent.ID.getNextAxisId();
    assertTrue(nextId > Axis.MAX_DEFAULT_AXIS_ID, "Next ID should be greater than Axis.MAX_DEFAULT_AXIS_ID");
  }

  @Test
  public void testGetById() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.UNKNOWN, 1, "SOMETHING_ELSE_1");
    assertEquals(id, InputComponent.ID.get(ComponentType.UNKNOWN, 1));
  }

  @Test
  public void testGetByName() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    assertEquals(id, InputComponent.ID.get("BUTTON_1"));
  }
}
