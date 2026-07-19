package io.cutehat.gabby.domain.transfer;

import java.time.Instant;
import java.util.Objects;

public final class TransferMeta {
    private long bytesStreamed;

    public long getBytesStreamed() {
        return bytesStreamed;
    }

    private Instant lastModifiedAt;

    public TransferMeta(long bytesStreamed) {
        this.bytesStreamed = bytesStreamed;
    }

    public long addBytesStreamed(long byteCount) {
        bytesStreamed += byteCount;
        lastModifiedAt = Instant.now();
        return bytesStreamed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TransferMeta) obj;
        return this.bytesStreamed == that.bytesStreamed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bytesStreamed);
    }

    @Override
    public String toString() {
        return "TransferMeta[" +
                "bytesStreamed=" + bytesStreamed + ']';
    }

}
