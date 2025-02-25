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
    assertEquals(id1, id2);
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
    int nextId = InputComponent.ID.getNextId();
    InputComponent.ID newId = new InputComponent.ID(ComponentType.BUTTON, nextId, "NEW_BUTTON");
    assertEquals(nextId, newId.id);
  }

  @Test
  public void testNextIdLeavesRoomForDefaultComponents() {
    int nextId = InputComponent.ID.getNextId();
    assertTrue(nextId > Axis.MAX_AXIS_ID, "Next ID should be greater than Axis.MAX_AXIS_ID");
  }

  @Test
  public void testGetById() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    assertEquals(id, InputComponent.ID.get(1));
  }

  @Test
  public void testGetByName() {
    InputComponent.ID id = new InputComponent.ID(ComponentType.BUTTON, 1, "BUTTON_1");
    assertEquals(id, InputComponent.ID.get("BUTTON_1"));
  }
}