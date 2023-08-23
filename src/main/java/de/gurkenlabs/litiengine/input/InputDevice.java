package de.gurkenlabs.litiengine.input;

import java.util.*;
import java.util.function.Function;

public final class InputDevice {
  private final UUID instance;
  private final UUID product;
  private final String instanceName;
  private final String productName;

  private final Function<InputDevice, float[]> pollCallback;

  private final LinkedHashMap<InputComponent, Float> components = new LinkedHashMap<>();

  public InputDevice(UUID instance, UUID product, String instanceName, String productName, Function<InputDevice, float[]> pollCallback) {
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

  public Collection<InputComponent> getComponents() {
    return components.keySet();
  }

  public void addComponents(Collection<InputComponent> components) {
    for (var component : components) {
      this.components.put(component, 0f);
    }
  }

  public void poll() {
    var polledData = this.pollCallback.apply(this);

    var componentList = new ArrayList<>(components.keySet());
    for (var i = 0; i < polledData.length; i++) {
      var component = componentList.get(i);
      this.components.put(component, polledData[i]);
    }
  }

  public float getData(InputComponent component) {
    return this.components.get(component);
  }
}
