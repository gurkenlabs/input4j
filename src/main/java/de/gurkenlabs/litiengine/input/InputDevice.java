package de.gurkenlabs.litiengine.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public final class InputDevice {
  private final UUID instance;
  private final UUID product;
  private final String instanceName;
  private final String productName;

  private final Consumer<InputDevice> pollCallback;

  private final ArrayList<DeviceComponent> components = new ArrayList<>();

  public InputDevice(UUID instance, UUID product, String instanceName, String productName, Consumer<InputDevice> pollCallback) {
    this.instance = instance;
    this.product = product;
    this.instanceName = instanceName;
    this.productName = productName;
    this.pollCallback = pollCallback;
  }

  public UUID getInstance() {
    return instance;
  }

  public UUID getProduct() {
    return product;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public String getProductName() {
    return productName;
  }

  public ArrayList<DeviceComponent> getComponents() {
    return components;
  }

  public void addComponents(Collection<DeviceComponent> component) {
    this.components.addAll(component);
  }

  public void poll() {
    this.pollCallback.accept(this);
  }
}
