package io.cutehat.gabby.api.protocol.payload;

import io.cutehat.gabby.domain.transfer.TransferEntity;
import io.cutehat.gabby.domain.transfer.TransferStatus;

import java.time.Instant;

public record TransferDTO(String id,
                          TransferStatus status,
                          String from,
                          String to,
                          FileMeta fileMeta,
                          Instant validUntil) {

    public static TransferDTO from(TransferEntity entity) {
        return new TransferDTO(
                entity.getId(),
                entity.getStatus(),
                entity.getFrom(),
                entity.getTo(),
                entity.getFileMeta(),
                entity.getValidUntil()
        );
    }
}
