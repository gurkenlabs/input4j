package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.lang.foreign.MemorySegment;

public class IOHIDDevice {
    long deviceAddress;
    int vendorId;
    int productId;
    String productName;
    String manufacturer;
    String transport;
    int usage;
}
