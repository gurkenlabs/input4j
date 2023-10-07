package de.gurkenlabs.input4j.foreign.linux;

import java.io.Closeable;
import java.util.logging.Level;
import java.util.logging.Logger;

class LinuxEventDevice implements Closeable {
  private static final Logger log = Logger.getLogger(LinuxEventDevice.class.getName());


  private final String filename;

  private final int fileDescriptor;

  private final String name;
  private input_id id;

  private int version;

  private boolean hasReadWriteAccess = true;

  public LinuxEventDevice(String filename, NativeContext nativeContext) {
    this.filename = filename;

    // attempt to open the device with read/write access
    var fd = Linux.open(nativeContext, this.filename, Linux.O_RDWR);
    if (fd == Linux.ERROR) {
      hasReadWriteAccess = false;
      fd = Linux.open(nativeContext, this.filename, Linux.O_RDONLY);
      if (fd == Linux.ERROR) {
        this.fileDescriptor = fd;
        this.name = null;
        log.log(Level.SEVERE, "Could not open linux event device '" + filename + "'");
        return;
      }
    }

    this.fileDescriptor = fd;
    this.name = Linux.getEventDeviceName(nativeContext, this.fileDescriptor);
    this.id = Linux.getEventDeviceId(nativeContext, this.fileDescriptor);
    this.version = Linux.getEventDeviceVersion(nativeContext, this.fileDescriptor);
  }

  @Override
  public void close() {

  }

  public String getFilename() {
    return filename;
  }

  public String getName() {
    return name;
  }
}
