package de.gurkenlabs.input4j.foreign.linux;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;

import static org.junit.jupiter.api.Assertions.*;

public class LinuxForceFeedbackStructTests {

  @Test
  public void testFfReplay() {
    try (var memorySession = Arena.ofConfined()) {
      var replay = new ff_replay();
      replay.length = 1000;
      replay.delay = 500;

      var segment = memorySession.allocate(ff_replay.$LAYOUT);
      replay.write(segment);

      var replayFromMemory = ff_replay.read(segment);
      assertEquals(replay.length, replayFromMemory.length);
      assertEquals(replay.delay, replayFromMemory.delay);
    }
  }

  @Test
  public void testFfTrigger() {
    try (var memorySession = Arena.ofConfined()) {
      var trigger = new ff_trigger();
      trigger.button = 0;
      trigger.interval = 100;

      var segment = memorySession.allocate(ff_trigger.$LAYOUT);
      trigger.write(segment);

      var triggerFromMemory = ff_trigger.read(segment);
      assertEquals(trigger.button, triggerFromMemory.button);
      assertEquals(trigger.interval, triggerFromMemory.interval);
    }
  }

  @Test
  public void testFfRumbleEffect() {
    try (var memorySession = Arena.ofConfined()) {
      var rumble = new ff_rumble_effect();
      rumble.strong_magnitude = (short) 40000;
      rumble.weak_magnitude = (short) 20000;

      var segment = memorySession.allocate(ff_rumble_effect.$LAYOUT);
      rumble.write(segment);

      var rumbleFromMemory = ff_rumble_effect.read(segment);
      assertEquals(rumble.strong_magnitude, rumbleFromMemory.strong_magnitude);
      assertEquals(rumble.weak_magnitude, rumbleFromMemory.weak_magnitude);
    }
  }

  @Test
  public void testFfEffect() {
    try (var memorySession = Arena.ofConfined()) {
      var effect = new ff_effect();
      effect.type = Linux.FF_RUMBLE;
      effect.id = -1;
      effect.direction = 0;

      effect.trigger = new ff_trigger();
      effect.trigger.button = 0;
      effect.trigger.interval = 0;

      effect.replay = new ff_replay();
      effect.replay.length = 0;
      effect.replay.delay = 0;

      effect.rumble = new ff_rumble_effect();
      effect.rumble.strong_magnitude = (short) 30000;
      effect.rumble.weak_magnitude = (short) 15000;

      var segment = memorySession.allocate(ff_effect.$LAYOUT);
      effect.write(segment);

      var effectFromMemory = ff_effect.read(segment);
      assertEquals(effect.type, effectFromMemory.type);
      assertEquals(effect.id, effectFromMemory.id);
      assertEquals(effect.direction, effectFromMemory.direction);
      assertEquals(effect.trigger.button, effectFromMemory.trigger.button);
      assertEquals(effect.trigger.interval, effectFromMemory.trigger.interval);
      assertEquals(effect.replay.length, effectFromMemory.replay.length);
      assertEquals(effect.replay.delay, effectFromMemory.replay.delay);
      assertEquals(effect.rumble.strong_magnitude, effectFromMemory.rumble.strong_magnitude);
      assertEquals(effect.rumble.weak_magnitude, effectFromMemory.rumble.weak_magnitude);
    }
  }

  @Test
  public void testFfEffectLayoutSize() {
    long expectedSize = 2 + 2 + 2 + ff_trigger.$LAYOUT.byteSize() + ff_replay.$LAYOUT.byteSize() + (24 * 2);
    assertEquals(expectedSize, ff_effect.$LAYOUT.byteSize(),
        "ff_effect layout size should match kernel struct");
  }

  @Test
  public void testMagnitudeConversion() {
    float intensity1 = 0.0f;
    assertEquals(0, (int) (intensity1 * 65535));

    float intensity2 = 0.5f;
    assertEquals(32767, (int) (intensity2 * 65535));

    float intensity3 = 1.0f;
    assertEquals(65535, (int) (intensity3 * 65535));

    float intensity4 = 0.75f;
    int magnitude = (int) (intensity4 * 65535);
    assertTrue(magnitude > 40000 && magnitude < 50000);
  }
}
