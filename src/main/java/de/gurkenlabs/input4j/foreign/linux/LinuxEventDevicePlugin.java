package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.AbstractInputDevicePlugin;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.foreign.NativeHelper;

import java.awt.*;
import java.io.File;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code LinuxEventDevicePlugin} class is responsible for managing Linux event devices.
 * It initializes and adds them to the collection of devices.
 *
 * TODO: Test this on raspberrypi3b with SNES USB controller and old controller
 */
public class LinuxEventDevicePlugin extends AbstractInputDevicePlugin implements NativeContext {
  private static final Logger log = Logger.getLogger(LinuxEventDevicePlugin.class.getName());
  private static final VarHandle errnoHandle;
  private static final MethodHandle strerror;
  private static final MethodHandle select;
  private static final MethodHandle read;
  private static final MethodHandle ioctl;
  private static final MethodHandle open;
  private static final int O_RDONLY = 0;
  private static final int EVIOCGRAB = 0x40044590;
  private static final int EV_SYN = 0x00;
  private static final int SYN_MT_REPORT = 0x02;
  private static final int SYN_DROPPED = 0x03;
  private static final int EV_MSC = 0x04;
  private static final int MSC_RAW = 0x03;
  private static final int MSC_SCAN = 0x04;

  private final Arena memoryArena = Arena.ofConfined();
  private final MemorySegment capturedState;
  private Collection<InputDevice> devices = ConcurrentHashMap.newKeySet();
  private volatile boolean stop = false;

  static {
    StructLayout capturedStateLayout = Linker.Option.captureStateLayout();
    errnoHandle = capturedStateLayout.varHandle(MemoryLayout.PathElement.groupElement("errno"));
    strerror = NativeHelper.downcallHandle("strerror", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
    select = NativeHelper.downcallHandle("select", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    read = NativeHelper.downcallHandle("read", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
    ioctl = NativeHelper.downcallHandle("ioctl", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    open = NativeHelper.downcallHandle("open", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
  }

  public LinuxEventDevicePlugin() {
    this.capturedState = this.memoryArena.allocate(Linker.Option.captureStateLayout());
  }

  @Override
  public void internalInitDevices(Frame owner) {
    enumEventDevices();
  }

  private void enumEventDevices() {
    final File dev = new File("/dev/input");
    File[] eventDeviceFiles = dev.listFiles((File dir, String name) -> name.startsWith("event"));
    if (eventDeviceFiles == null) {
      log.log(Level.SEVERE, "No event devices found");
      return;
    } else {
      Arrays.sort(eventDeviceFiles, Comparator.comparing(File::getName));
    }

    for (var eventDeviceFile : eventDeviceFiles) {
      LinuxEventDevice device = new LinuxEventDevice(eventDeviceFile.getAbsolutePath(), this);
      log.log(Level.INFO, "Found input device: " + device.getFilename() + " - " + device.getName());
      new Thread(() -> printEvents(device)).start();
    }
  }

  private void printEvents(LinuxEventDevice device) {
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment ev = arena.allocate(input_event.$LAYOUT);
      MemorySegment rdfs = arena.allocate(ValueLayout.JAVA_INT.byteSize());
      int fd = getFileDescriptor(device);

      if (fd < 0) {
        log.log(Level.SEVERE, "Failed to open device: " + device.getFilename());
        return;
      }

      while (!stop) {
        FD_ZERO(rdfs);
        FD_SET(fd, rdfs);

        select.invoke(fd + 1, rdfs, MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL);
        if (stop) break;

        int rd = (int) read.invoke(fd, ev, ev.byteSize());
        if (rd < input_event.$LAYOUT.byteSize()) {
          log.log(Level.SEVERE, "Expected " + input_event.$LAYOUT.byteSize() + " bytes, got " + rd);
          return;
        }

        for (int i = 0; i < rd / input_event.$LAYOUT.byteSize(); i++) {
          input_event event = input_event.read(ev.asSlice(i * input_event.$LAYOUT.byteSize()));
          logEvent(event);
        }
      }

      ioctl.invoke(fd, EVIOCGRAB, MemorySegment.NULL);
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private int getFileDescriptor(LinuxEventDevice device) {
    try {
      MemorySegment filename = memoryArena.allocateFrom(device.getFilename());
      return (int) open.invoke(filename, O_RDONLY);
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return -1;
    }
  }

  private void logEvent(input_event event) {
    int type = event.type;
    int code = event.code;
    log.log(Level.INFO, "Event: time " + event.time.tv_sec + "." + event.time.tv_usec + ", type " + type + ", code " + code + ", value " + event.value);
  }

  @Override
  public Collection<InputDevice> getAll() {
    return this.devices;
  }

  @Override
  public void close() {
    stop = true;
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
      return ((MemorySegment) strerror.invoke(this.getErrorNo())).reinterpret(Long.MAX_VALUE).getString(0, Charset.defaultCharset());
    } catch (Throwable e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
    return null;
  }

  private void FD_ZERO(MemorySegment set) {
    set.set(ValueLayout.JAVA_INT, 0, 0);
  }

  private void FD_SET(int fd, MemorySegment set) {
    set.set(ValueLayout.JAVA_INT, 0, set.get(ValueLayout.JAVA_INT, 0) | (1 << fd));
  }
}