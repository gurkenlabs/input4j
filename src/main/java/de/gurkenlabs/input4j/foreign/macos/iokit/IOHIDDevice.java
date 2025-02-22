package de.gurkenlabs.input4j.foreign.macos.iokit;

public class IOHIDDevice {
    long deviceAddress;
    int vendorId;
    int productId;
    String productName;
    String manufacturer;
    String transport;
    int usage;

    @Override
    public String toString() {
        return "IOHIDDevice{" +
                "deviceAddress=" + deviceAddress +
                ", vendorId=" + vendorId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", transport='" + transport + '\'' +
                ", usage=" + usage +
                '}';
    }
}
