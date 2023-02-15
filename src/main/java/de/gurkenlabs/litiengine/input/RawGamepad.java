package de.gurkenlabs.litiengine.input;

import java.util.UUID;

public record RawGamepad(UUID product, UUID instance, String productName, String instanceName) {
}
