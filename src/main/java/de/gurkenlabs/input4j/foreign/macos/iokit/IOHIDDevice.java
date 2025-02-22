package de.gurkenlabs.input4j.foreign.macos.iokit;

import java.util.HexFormat;

public class IOHIDDevice {
    long deviceAddress;
    int vendorId;
    int productId;
    String productName;
    String manufacturer;
    String transport;
    int usage;
    int usagePage;

    @Override
    public String toString() {
        return "address: " + deviceAddress +
                ", product: '" + productName + "' ("+ String.format("0X%02X", productId) +")" +
                ", vendor: '" + manufacturer + "' ("+ String.format("0X%02X", vendorId) +")" +
                ", transport: '" + transport + '\'' +
                ", usage: " + String.format("0X%02X", usage) + " (page: " + String.format("0X%02X", usagePage + ")");
    }
}
