package de.gurkenlabs.input4j.foreign.linux;

import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDevicePlugin;

import java.io.File;
import java.lang.foreign.Arena;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LinuxEventDevicePlugin implements InputDevicePlugin {
  private static final Logger log = Logger.getLogger(LinuxEventDevicePlugin.class.getName());

  private final Arena memoryArena = Arena.ofConfined();

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
      LinuxEventDevice device = new LinuxEventDevice(eventDeviceFile.getAbsolutePath(), this.memoryArena);
      log.log(Level.INFO, "Found input device: " + device.getFilename());
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
}
