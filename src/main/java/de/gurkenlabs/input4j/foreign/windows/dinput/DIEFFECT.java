package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.*;

/**
 * Describes the parameters for an effect.
 * This structure is used with the IDirectInputDevice8::CreateEffect, IDirectInputEffect::SetParameters,
 * and IDirectInputEffect::GetParameters methods.
 */
final class DIEFFECT {

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_INT.withName("dwSize"),
          JAVA_INT.withName("dwFlags"),
          JAVA_INT.withName("dwDuration"),
          JAVA_INT.withName("dwSamplePeriod"),
          JAVA_INT.withName("dwGain"),
          JAVA_INT.withName("dwTriggerButton"),
          JAVA_INT.withName("dwTriggerRepeatInterval"),
          JAVA_INT.withName("cAxes"),
          ADDRESS.withName("rgdwAxes"),
          ADDRESS.withName("rglDirection"),
          ADDRESS.withName("lpEnvelope"),
          JAVA_INT.withName("cbTypeSpecificParams"),
          ADDRESS.withName("lpvTypeSpecificParams").withByteAlignment(4),
          JAVA_INT.withName("dwStartDelay")
  ).withName("DIEFFECT");

  /**
   * Size of this structure, in bytes. This member must be initialized before the structure is used.
   */
  public int dwSize;

  public DIEFFECT() {
    this.dwSize = (int) $LAYOUT.byteSize();
  }

  /**
   * Flags that specify which portions of this structure contain valid data.
   */
  public int dwFlags;

  /**
   * Duration of the effect, in microseconds.
   */
  public int dwDuration;

  /**
   * Sample period at which the effect should be played, in microseconds.
   */
  public int dwSamplePeriod;

  /**
   * Gain of the effect, in the range from 0 through 10000.
   */
  public int dwGain;

  /**
   * Button identifier that triggers the effect.
   */
  public int dwTriggerButton;

  /**
   * Interval between repetitions of the effect when the trigger button is held, in microseconds.
   */
  public int dwTriggerRepeatInterval;

  /**
   * Number of axes involved in the effect.
   */
  public int cAxes;

  /**
   * Reserved, must be NULL.
   */
  public MemorySegment rgdwAxes;

  /**
   * Direction coordinate values.
   */
  public MemorySegment rglDirection;

  /**
   * Envelope for the effect.
   */
  public MemorySegment lpEnvelope;

  /**
   * Type specific parameters for the effect.
   */
  public int cbTypeSpecificParams;

  /**
   * Pointer to type specific effect parameters.
   */
  public MemorySegment lpvTypeSpecificParams;

  /**
   * Start offset for the effect.
   */
  public int dwStartDelay;

  static final VarHandle VH_dwSize = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSize"));
  static final VarHandle VH_dwFlags = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwFlags"));
  static final VarHandle VH_dwDuration = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwDuration"));
  static final VarHandle VH_dwSamplePeriod = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwSamplePeriod"));
  static final VarHandle VH_dwGain = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwGain"));
  static final VarHandle VH_dwTriggerButton = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwTriggerButton"));
  static final VarHandle VH_dwTriggerRepeatInterval = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwTriggerRepeatInterval"));
  static final VarHandle VH_cAxes = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("cAxes"));
  static final VarHandle VH_rgdwAxes = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("rgdwAxes"));
  static final VarHandle VH_rglDirection = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("rglDirection"));
  static final VarHandle VH_lpEnvelope = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lpEnvelope"));
  static final VarHandle VH_cbTypeSpecificParams = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("cbTypeSpecificParams"));
  static final VarHandle VH_lpvTypeSpecificParams = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("lpvTypeSpecificParams"));
  static final VarHandle VH_dwStartDelay = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("dwStartDelay"));

  public static DIEFFECT read(MemorySegment segment) {
    var data = new DIEFFECT();
    data.dwSize = (int) VH_dwSize.get(segment, 0);
    data.dwFlags = (int) VH_dwFlags.get(segment, 0);
    data.dwDuration = (int) VH_dwDuration.get(segment, 0);
    data.dwSamplePeriod = (int) VH_dwSamplePeriod.get(segment, 0);
    data.dwGain = (int) VH_dwGain.get(segment, 0);
    data.dwTriggerButton = (int) VH_dwTriggerButton.get(segment, 0);
    data.dwTriggerRepeatInterval = (int) VH_dwTriggerRepeatInterval.get(segment, 0);
    data.cAxes = (int) VH_cAxes.get(segment, 0);
    data.rgdwAxes = (MemorySegment) VH_rgdwAxes.get(segment, 0);
    data.rglDirection = (MemorySegment) VH_rglDirection.get(segment, 0);
    data.lpEnvelope = (MemorySegment) VH_lpEnvelope.get(segment, 0);
    data.cbTypeSpecificParams = (int) VH_cbTypeSpecificParams.get(segment, 0);
    data.lpvTypeSpecificParams = (MemorySegment) VH_lpvTypeSpecificParams.get(segment, 0);
    data.dwStartDelay = (int) VH_dwStartDelay.get(segment, 0);
    return data;
  }

  public static void write(MemorySegment segment, DIEFFECT data) {
    VH_dwSize.set(segment, 0, data.dwSize);
    VH_dwFlags.set(segment, 0, data.dwFlags);
    VH_dwDuration.set(segment, 0, data.dwDuration);
    VH_dwSamplePeriod.set(segment, 0, data.dwSamplePeriod);
    VH_dwGain.set(segment, 0, data.dwGain);
    VH_dwTriggerButton.set(segment, 0, data.dwTriggerButton);
    VH_dwTriggerRepeatInterval.set(segment, 0, data.dwTriggerRepeatInterval);
    VH_cAxes.set(segment, 0, data.cAxes);
    VH_rgdwAxes.set(segment, 0, data.rgdwAxes);
    VH_rglDirection.set(segment, 0, data.rglDirection);
    VH_lpEnvelope.set(segment, 0, data.lpEnvelope);
    VH_cbTypeSpecificParams.set(segment, 0, data.cbTypeSpecificParams);
    VH_lpvTypeSpecificParams.set(segment, 0, data.lpvTypeSpecificParams);
    VH_dwStartDelay.set(segment, 0, data.dwStartDelay);
  }
}
