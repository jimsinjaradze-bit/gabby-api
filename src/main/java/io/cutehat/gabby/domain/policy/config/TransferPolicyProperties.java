package io.cutehat.gabby.domain.policy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "transfer.policy")
public record TransferPolicyProperties(Integer requestValidForSeconds,
                                       Integer maxChunkSizeInBytes) {
}
