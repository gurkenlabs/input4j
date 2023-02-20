package de.gurkenlabs.litiengine.input.windows;

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
}
