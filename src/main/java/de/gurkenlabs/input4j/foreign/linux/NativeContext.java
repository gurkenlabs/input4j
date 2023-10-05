package de.gurkenlabs.input4j.foreign.linux;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

interface NativeContext {
  Arena getArena();

  /**
   * Get the capture state that needs to be passed as first argument for downcall handles that require a
   * captureCallState (e.g. for native error handling via "errno").
   *
   * @return The captured state memory segment of this native context.
   */
  MemorySegment getCapturedState();

  int getErrorNo();

  String getError();
}
