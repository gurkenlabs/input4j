package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.util.Arrays;

/**
 * The `DIEFFECTTYPE` enum represents various types of DirectInput effects.
 * Each enum constant corresponds to a specific effect type code.
 */
enum DIEFFECTTYPE {
  DIEFT_NONE(0x00000000),
  DIEFT_CONSTANTFORCE(0x00000001),
  DIEFT_RAMPFORCE(0x00000002),
  DIEFT_PERIODIC(0x00000003),
  DIEFT_CONDITION(0x00000004),
  DIEFT_CUSTOMFORCE(0x00000005),
  DIEFT_HARDWARE(0x000000FF);

  private final int effType;

  DIEFFECTTYPE(int effType) {
    this.effType = effType;
  }

  /**
   * Converts an effect type code to its corresponding {@link DIEFFECTTYPE} enum constant.
   *
   * @param dwEffType The effect type code.
   * @return The corresponding {@link DIEFFECTTYPE} enum constant, or {@code DIEFT_NONE} if no match is found.
   */
  public static DIEFFECTTYPE fromDwEffType(int dwEffType) {
    return Arrays.stream(DIEFFECTTYPE.values()).filter(x -> x.effType == dwEffType).findFirst().orElse(DIEFT_NONE);
  }

  /**
   * Gets the effect type code associated with this enum constant.
   *
   * @return The effect type code.
   */
  public int getEffType() {
    return effType;
  }
}