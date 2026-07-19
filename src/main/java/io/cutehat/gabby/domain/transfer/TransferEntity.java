package io.cutehat.gabby.domain.transfer;

import io.cutehat.gabby.api.protocol.payload.FileMeta;

import java.time.Instant;
import java.util.Objects;

public final class TransferEntity {
    private final String id;
    private final String from;
    private final String to;
    private final FileMeta fileMeta;
    private final TransferMeta transferMeta;
    private TransferStatus status;
    private final Instant validUntil;
    private final Instant createdAt;
    private Instant lastModifiedAt;

    public TransferEntity(String id,
                          String from,
                          String to,
                          FileMeta fileMeta,
                          TransferMeta transferMeta,
                          TransferStatus status,
                          Instant validUntil,
                          Instant createdAt,
                          Instant lastModifiedAt) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.fileMeta = fileMeta;
        this.transferMeta = transferMeta;
        this.status = status;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
        this.lastModifiedAt = Instant.now();
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public TransferMeta getTransferMeta() {
        return transferMeta;
    }

    public FileMeta getFileMeta() {
        return fileMeta;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TransferEntity) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.from, that.from) &&
                Objects.equals(this.to, that.to) &&
                Objects.equals(this.fileMeta, that.fileMeta) &&
                Objects.equals(this.transferMeta, that.transferMeta) &&
                Objects.equals(this.status, that.status) &&
                Objects.equals(this.validUntil, that.validUntil) &&
                Objects.equals(this.createdAt, that.createdAt) &&
                Objects.equals(this.lastModifiedAt, that.lastModifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, from, to, fileMeta, transferMeta, status, validUntil, createdAt, lastModifiedAt);
    }

    @Override
    public String toString() {
        return "TransferEntity[" +
                "id=" + id + ", " +
                "from=" + from + ", " +
                "to=" + to + ", " +
                "fileMeta=" + fileMeta + ", " +
                "transferMeta=" + transferMeta + ", " +
                "status=" + status + ", " +
                "validUntil=" + validUntil + ", " +
                "createdAt=" + createdAt + ", " +
                "lastModifiedAt=" + lastModifiedAt + ']';
    }

}