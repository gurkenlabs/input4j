package de.gurkenlabs.litiengine.input;

import java.util.UUID;

public final class RawGamepad {
  private final UUID instance;
  private final UUID product;
  private final String instanceName;
  private final String productName;

  public RawGamepad(UUID instance, UUID product, String instanceName, String productName) {
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

}
