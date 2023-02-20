package de.gurkenlabs.litiengine.input;

import java.util.ArrayList;
import java.util.UUID;

public final class InputDevice {
  private final UUID instance;
  private final UUID product;
  private final String instanceName;
  private final String productName;

  private final ArrayList<DeviceComponent> components = new ArrayList<>();

  public InputDevice(UUID instance, UUID product, String instanceName, String productName) {
    this.instance = instance;
    this.product = product;
    this.instanceName = instanceName;
    this.productName = productName;
  }

  public UUID instance() {
    return instance;
  }

  public UUID product() {
    return product;
  }

  public String instanceName() {
    return instanceName;
  }

  public String productName() {
    return productName;
  }

  public ArrayList<DeviceComponent> getComponents() {
    return components;
  }

  public void addComponents(ArrayList<DeviceComponent> component) {
    this.components.addAll(component);
  }
}
