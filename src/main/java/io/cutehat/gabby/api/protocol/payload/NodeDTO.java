package io.cutehat.gabby.api.protocol.payload;

import java.time.Instant;

public record NodeDTO(String name, Instant joinedAt) {
}
