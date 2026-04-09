package de.gurkenlabs.input4j;

/**
 * Represents the type of battery in a game controller.
 */
public enum BatteryType {
  /** The device is connected via USB and has no battery. */
  WIRED,
  /** The device is not connected. */
  DISCONNECTED,
  /** The device uses alkaline batteries. */
  ALKALINE,
  /** The device uses nickel-metal hydride (NiMH) batteries. */
  NIMH,
  /** The battery type is unknown or unavailable. */
  UNKNOWN
}