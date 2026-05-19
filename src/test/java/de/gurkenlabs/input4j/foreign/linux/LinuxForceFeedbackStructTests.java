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

  @Test
  public void testFfEnvelope() {
    try (var memorySession = Arena.ofConfined()) {
      var envelope = new ff_envelope();
      envelope.attack_length = 100;
      envelope.attack_level = 32767;
      envelope.fade_length = 200;
      envelope.fade_level = 16384;

      var segment = memorySession.allocate(ff_envelope.$LAYOUT);
      envelope.write(segment);

      var envelopeFromMemory = ff_envelope.read(segment);
      assertEquals(envelope.attack_length, envelopeFromMemory.attack_length);
      assertEquals(envelope.attack_level, envelopeFromMemory.attack_level);
      assertEquals(envelope.fade_length, envelopeFromMemory.fade_length);
      assertEquals(envelope.fade_level, envelopeFromMemory.fade_level);
    }
  }

  @Test
  public void testFfPeriodicEffect() {
    try (var memorySession = Arena.ofConfined()) {
      var periodic = new ff_periodic_effect();
      periodic.waveform = Linux.FF_SINE;
      periodic.period = 50;
      periodic.magnitude = 30000;
      periodic.offset = 0;
      periodic.phase = 0;
      periodic.envelope = new ff_envelope();
      periodic.envelope.attack_length = 0;
      periodic.envelope.attack_level = 0;
      periodic.envelope.fade_length = 0;
      periodic.envelope.fade_level = 0;

      var segment = memorySession.allocate(ff_periodic_effect.$LAYOUT);
      periodic.write(segment);

      var periodicFromMemory = ff_periodic_effect.read(segment);
      assertEquals(periodic.waveform, periodicFromMemory.waveform);
      assertEquals(periodic.period, periodicFromMemory.period);
      assertEquals(periodic.magnitude, periodicFromMemory.magnitude);
      assertEquals(periodic.offset, periodicFromMemory.offset);
      assertEquals(periodic.phase, periodicFromMemory.phase);
      assertEquals(periodic.envelope.attack_length, periodicFromMemory.envelope.attack_length);
      assertEquals(periodic.envelope.attack_level, periodicFromMemory.envelope.attack_level);
      assertEquals(periodic.envelope.fade_length, periodicFromMemory.envelope.fade_length);
      assertEquals(periodic.envelope.fade_level, periodicFromMemory.envelope.fade_level);
    }
  }

  @Test
  public void testFfEffectWithPeriodic() {
    try (var memorySession = Arena.ofConfined()) {
      var effect = new ff_effect();
      effect.type = Linux.FF_PERIODIC;
      effect.id = -1;
      effect.direction = 0;

      effect.trigger = new ff_trigger();
      effect.trigger.button = 0;
      effect.trigger.interval = 0;

      effect.replay = new ff_replay();
      effect.replay.length = 0;
      effect.replay.delay = 0;

      effect.periodic = new ff_periodic_effect();
      effect.periodic.waveform = Linux.FF_SINE;
      effect.periodic.period = 50;
      effect.periodic.magnitude = 20000;
      effect.periodic.offset = 0;
      effect.periodic.phase = 0;
      effect.periodic.envelope = new ff_envelope();
      effect.periodic.envelope.attack_length = 0;
      effect.periodic.envelope.attack_level = 0;
      effect.periodic.envelope.fade_length = 0;
      effect.periodic.envelope.fade_level = 0;

      var segment = memorySession.allocate(ff_effect.$LAYOUT);
      effect.write(segment);

      var effectFromMemory = ff_effect.read(segment);
      assertEquals(effect.type, effectFromMemory.type);
      assertEquals(effect.id, effectFromMemory.id);
      assertEquals(effect.periodic.waveform, effectFromMemory.periodic.waveform);
      assertEquals(effect.periodic.period, effectFromMemory.periodic.period);
      assertEquals(effect.periodic.magnitude, effectFromMemory.periodic.magnitude);
    }
  }

  @Test
  public void testFfConstants() {
    assertEquals(0x50, Linux.FF_RUMBLE);
    assertEquals(0x51, Linux.FF_PERIODIC);
    assertEquals(0x57, Linux.FF_EFFECT_MAX);
    assertEquals(0x57, Linux.FF_RAMP);
    assertEquals(0x5a, Linux.FF_SINE);
    assertEquals(0x60, Linux.FF_GAIN);
    assertEquals(0x7f, Linux.FF_MAX);
    assertEquals(0x80, Linux.FF_CNT);
  }

  @Test
  public void testSineFallbackMagnitudeMatchesKernelCompatEffect() {
    float strong = 1.0f;
    float weak = 1.0f;
    int magnitude = (int) (strong * 65535 / 3 + weak * 65535 / 6);
    assertEquals(32767, magnitude);

    strong = 1.0f;
    weak = 0.0f;
    magnitude = (int) (strong * 65535 / 3 + weak * 65535 / 6);
    assertEquals(21845, magnitude);

    strong = 0.0f;
    weak = 1.0f;
    magnitude = (int) (strong * 65535 / 3 + weak * 65535 / 6);
    assertEquals(10922, magnitude);

    strong = 0.5f;
    weak = 0.5f;
    magnitude = (int) (strong * 65535 / 3 + weak * 65535 / 6);
    assertEquals(16383, magnitude);
  }
}
