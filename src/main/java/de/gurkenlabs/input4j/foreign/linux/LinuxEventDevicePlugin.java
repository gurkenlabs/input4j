package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDevicePlugin;
import de.gurkenlabs.input4j.foreign.NativeHelper;

import java.io.File;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LinuxEventDevicePlugin implements InputDevicePlugin, NativeContext {
  private static final Logger log = Logger.getLogger(LinuxEventDevicePlugin.class.getName());
  private static final VarHandle errnoHandle;

  private static final MethodHandle strerror;

  private final Arena memoryArena = Arena.ofConfined();

  private final MemorySegment capturedState;

  static {
    StructLayout capturedStateLayout = Linker.Option.captureStateLayout();
    errnoHandle = capturedStateLayout.varHandle(MemoryLayout.PathElement.groupElement("errno"));

    // strerror C Standard Library function
    strerror = NativeHelper.downcallHandle("strerror",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
  }

  public LinuxEventDevicePlugin() {
    this.capturedState = this.memoryArena.allocate(Linker.Option.captureStateLayout());
  }

  @Override
  public void internalInitDevices() {
    enumEventDevices();
  }

  private void enumEventDevices() {
    final File dev = new File("/dev/input");

    File[] eventDeviceFiles = dev.listFiles((File dir, String name) -> name.startsWith("event"));
    if (eventDeviceFiles == null) {
      // TODO: log
      return;
    } else {
      Arrays.sort(eventDeviceFiles, Comparator.comparing(File::getName));
    }

    for (var eventDeviceFile : eventDeviceFiles) {
      LinuxEventDevice device = new LinuxEventDevice(eventDeviceFile.getAbsolutePath(), this);
      log.log(Level.INFO, "Found input device: " + device.getFilename() + " - " + device.getName());
    }
  }

  @Override
  public Collection<InputDevice> getAll() {
    return null;
  }

  @Override
  public void close() {
    memoryArena.close();
  }

  @Override
  public Arena getArena() {
    return this.memoryArena;
  }

  @Override
  public MemorySegment getCapturedState() {
    return this.capturedState;
  }

  @Override
  public int getErrorNo() {
    return (int) errnoHandle.get(getCapturedState());
  }

  @Override
  public String getError() {
    try {
      // Convert errno code to a string message
      return ((MemorySegment) strerror.invoke(this.getErrorNo()))
              .reinterpret(Long.MAX_VALUE).getUtf8String(0);
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }
}
