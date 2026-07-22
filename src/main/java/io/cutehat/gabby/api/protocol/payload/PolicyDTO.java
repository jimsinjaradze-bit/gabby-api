package io.cutehat.gabby.api.protocol.payload;

import io.cutehat.gabby.domain.policy.config.TransferPolicyProperties;

public record PolicyDTO(Integer maxChunkSizeInBytes) {

    public static PolicyDTO from(TransferPolicyProperties properties) {
        return new PolicyDTO(properties.maxChunkSizeInBytes());
    }

}
