package de.gurkenlabs.input4j;

/**
 * Represents the charge level of a game controller battery.
 */
public enum BatteryLevel {
  /** Battery is empty. */
  EMPTY,
  /** Battery is low. */
  LOW,
  /** Battery is at medium level. */
  MEDIUM,
  /** Battery is full. */
  FULL,
  /** Battery level is unknown or unavailable. */
  UNKNOWN
}