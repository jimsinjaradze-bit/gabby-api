package io.cutehat.gabby.domain.transfer;

import io.cutehat.gabby.api.protocol.payload.FileMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO clean up mechanism
@Component
public class TransferRepository {
    private static final Logger log = LoggerFactory.getLogger(TransferRepository.class);

    private final int MAX_CAPACITY = 64;
    private final Map<String, TransferEntity> transfers = new ConcurrentHashMap<>(MAX_CAPACITY, 1f);

    public TransferEntity createRequest(String from, String to, FileMeta fileMeta, int validForSeconds) {
        if (transfers.size() >= MAX_CAPACITY) {
            throw new IllegalStateException("Not accepting any more transfers");
        }

        validateNoActiveRequest(from, to);

        var id = generateId();
        var now = Instant.now();
        var validUntil = now.plus(validForSeconds, ChronoUnit.SECONDS);
        TransferEntity transfer = new TransferEntity(id, from, to, fileMeta, new TransferMeta(0), TransferStatus.REQUESTED, validUntil, now, now);

        transfers.put(id, transfer);
        return transfer;
    }

    public TransferEntity fetchOrThrow(String id) {
        return fetch(id).orElseThrow();
    }

    public Optional<TransferEntity> fetch(String id) {
        return Optional.ofNullable(transfers.get(id));
    }

    public TransferEntity setStatus(String id, TransferStatus status) {
        TransferEntity transfer = transfers.get(id);
        if (transfer == null) {
            throw new IllegalStateException("Transfer does not exist");
        }

        transfer.setStatus(status);
        return transfer;
    }

    public List<TransferEntity> getAllByParticipant(String participant) {
        return transfers.values()
                .stream()
                .filter(transferEntity ->
                        transferEntity.getFrom().equalsIgnoreCase(participant) || transferEntity.getTo().equalsIgnoreCase(participant))
                .toList();
    }

    public void removeAll(List<TransferEntity> toRemove) {
        toRemove.forEach(entity -> transfers.remove(entity.getId()));
    }

    public boolean existsByParticipantAndStatus(String participant,
                                                TransferStatus status) {
        return transfers
                .values()
                .stream()
                .anyMatch(transfer ->
                        (transfer.getFrom().equalsIgnoreCase(participant) || transfer.getTo().equalsIgnoreCase(participant))
                                && transfer.getStatus() == status
                );
    }

    public Optional<TransferEntity> getBySenderAndStatuses(String from,
                                                           List<TransferStatus> statuses) {
        return transfers
                .values()
                .stream()
                .filter(transfer -> transfer.getFrom().equalsIgnoreCase(from) && statuses.contains(transfer.getStatus()))
                .findAny();
    }

    private void validateNoActiveRequest(String from, String to) {
        transfers.forEach((id, transfer) -> {
            if (transfer.getStatus() == TransferStatus.REQUESTED && (transfer.getFrom().equalsIgnoreCase(from) || transfer.getTo().equalsIgnoreCase(to))) {
                log.error("participant already has transfer with id: {} in progress", id);
                throw new IllegalStateException("Participant already has transfer in progress");
            }
        });
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
