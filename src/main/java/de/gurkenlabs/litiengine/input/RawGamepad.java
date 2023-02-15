package de.gurkenlabs.litiengine.input;

import java.util.UUID;

public record RawGamepad(UUID instance, UUID product, String instanceName, String productName) {
}
