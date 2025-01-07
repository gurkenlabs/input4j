package de.gurkenlabs.input4j.foreign.linux;

import java.io.Closeable;
import java.util.logging.Logger;

class LinuxEventDevice implements Closeable {
  private static final Logger log = Logger.getLogger(LinuxEventDevice.class.getName());


  public final String filename;
  public final int fd;
  public final String name;
  public input_id id;

  public int version;

  public LinuxEventDevice(String filename, NativeContext nativeContext) {
    this.filename = filename;

    this.fd = Linux.open(nativeContext, this.filename);
    this.name = Linux.getEventDeviceName(nativeContext, this.fd);
    this.id = Linux.getEventDeviceId(nativeContext, this.fd);
    this.version = Linux.getEventDeviceVersion(nativeContext, this.fd);
  }

  @Override
  public void close() {
  }
}
