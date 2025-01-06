package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.util.Arrays;

/**
 * The `DI8DEVOBJECTTYPE` enum represents various types of DirectInput device objects.
 * Each enum constant corresponds to a specific object type GUID.
 */
enum DI8DEVOBJECTTYPE {
  XAxis(DIDEVICEOBJECTINSTANCE.GUID_XAxis),
  YAxis(DIDEVICEOBJECTINSTANCE.GUID_YAxis),
  ZAxis(DIDEVICEOBJECTINSTANCE.GUID_ZAxis),
  RxAxis(DIDEVICEOBJECTINSTANCE.GUID_RxAxis),
  RyAxis(DIDEVICEOBJECTINSTANCE.GUID_RyAxis),
  RzAxis(DIDEVICEOBJECTINSTANCE.GUID_RzAxis),
  Slider(DIDEVICEOBJECTINSTANCE.GUID_Slider),
  Button(DIDEVICEOBJECTINSTANCE.GUID_Button),
  Key(DIDEVICEOBJECTINSTANCE.GUID_Key),
  POV(DIDEVICEOBJECTINSTANCE.GUID_POV),
  Unknown(DIDEVICEOBJECTINSTANCE.GUID_Unknown);

  private final GUID typeGuid;

  DI8DEVOBJECTTYPE(GUID typeGuid) {
    this.typeGuid = typeGuid;
  }

  /**
   * Converts a GUID to its corresponding {@link DI8DEVOBJECTTYPE} enum constant.
   *
   * @param typeGuid The GUID of the device object type.
   * @return The corresponding {@link DI8DEVOBJECTTYPE} enum constant, or {@code null} if no match is found.
   */
  public static DI8DEVOBJECTTYPE from(GUID typeGuid) {
    //  The least-significant byte of the device type description code specifies the device type.
    return Arrays.stream(DI8DEVOBJECTTYPE.values()).filter(x -> x.typeGuid.equals(typeGuid)).findFirst().orElse(null);
  }

  /**
   * Gets the GUID associated with this enum constant.
   *
   * @return The GUID of the device object type.
   */
  public GUID getTypeGuid() {
    return typeGuid;
  }

  /**
   * Converts a POV value to a normalized float value.
   *
   * @param value The POV value.
   * @return The normalized float value.
   */
  public static float getPOV(int value){
    if ((value & 0xFFFF) == 0xFFFF)
      return 0.0f;
    // DirectInput returns POV directions in hundredths of degree clockwise from north
    int slice = 360*100/16;
    if (value >= 0 && value < slice)
      return 0.25f;
    else if (value < 3*slice)
      return 0.375f;
    else if (value < 5*slice)
      return 0.50f;
    else if (value < 7*slice)
      return 0.625f;
    else if (value < 9*slice)
      return 0.75f;
    else if (value < 11*slice)
      return 0.875f;
    else if (value < 13*slice)
      return 1.0f;
    else if (value < 15*slice)
      return 0.125f;
    else
      return 0.25f;
  }
}
