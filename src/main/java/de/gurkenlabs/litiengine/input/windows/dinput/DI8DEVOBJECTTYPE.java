package de.gurkenlabs.litiengine.input.windows.dinput;

import java.util.Arrays;

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

  public static DI8DEVOBJECTTYPE from(GUID typeGuid) {
    //  The least-significant byte of the device type description code specifies the device type.
    return Arrays.stream(DI8DEVOBJECTTYPE.values()).filter(x -> x.typeGuid.equals(typeGuid)).findFirst().orElse(null);
  }

  public GUID getTypeGuid() {
    return typeGuid;
  }

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
