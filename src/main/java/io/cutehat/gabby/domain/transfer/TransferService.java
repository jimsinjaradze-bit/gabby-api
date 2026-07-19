package io.cutehat.gabby.domain.transfer;

import io.cutehat.gabby.api.protocol.payload.FileMeta;
import io.cutehat.gabby.domain.discovery.NodeRepository;
import io.cutehat.gabby.domain.transfer.config.TransferPolicyProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.time.Instant;
import java.util.List;

@Service
public class TransferService {
    private final TransferRepository transferRepository;
    private final NodeRepository nodeRepository;
    private final TransferPolicyProperties policyProperties;

    public TransferService(TransferRepository transferRepository, NodeRepository nodeRepository, TransferPolicyProperties policyProperties) {
        this.transferRepository = transferRepository;
        this.nodeRepository = nodeRepository;
        this.policyProperties = policyProperties;
    }

    public List<TransferEntity> getAllByParticipant(String participant) {
        return transferRepository.getAllByParticipant(participant);
    }

    public TransferEntity get(String id) {
        return transferRepository.fetchOrThrow(id);
    }

    public TransferEntity requestTransfer(String from, String to, FileMeta fileMeta) {
        validateNodesAreConnected(from, to);
        validateFileMetaInformation(fileMeta);
        validateSingleActiveTransferFor(from);
        validateSingleActiveTransferFor(to);

        return transferRepository.createRequest(from, to, fileMeta, policyProperties.requestValidForSeconds());
    }

    public TransferEntity accept(String id, String recepient) {
        TransferEntity transfer = transferRepository.fetchOrThrow(id);

        if (!transfer.getTo().equalsIgnoreCase(recepient)) {
            throw new IllegalStateException("Not your transfer");
        }

        if (transfer.getValidUntil().isBefore(Instant.now())) {
            throw new IllegalStateException("Sorry, the transfer already expired");
        }

        if (transfer.getStatus() != TransferStatus.REQUESTED) {
            throw new IllegalStateException("Sorry, Transfer already transition to another status");
        }

        validateSingleActiveTransferFor(transfer.getFrom());
        validateSingleActiveTransferFor(transfer.getTo());
        transfer.setStatus(TransferStatus.ACCEPTED);

        return transfer;
    }

    public TransferEntity reject(String id, String recepient) {
        TransferEntity transfer = transferRepository.fetchOrThrow(id);

        if (!transfer.getTo().equalsIgnoreCase(recepient)) {
            throw new IllegalStateException("Not your transfer");
        }

        return transferRepository.setStatus(id, TransferStatus.REJECTED);
    }

    private void validateFileMetaInformation(FileMeta meta) {
        MimeType.valueOf(meta.mimeType()); // just making sure that mime type is valid, we don't whitelist it
    }

    private void validateNodesAreConnected(String from, String to) {
        if (!nodeRepository.exists(from) || !nodeRepository.exists(to)) {
            throw new IllegalStateException("Not all participant nodes are connected");
        }
    }

    private void validateSingleActiveTransferFor(String participant) {
        if (transferRepository.existsByParticipantAndStatus(participant, TransferStatus.IN_PROGRESS)) {
            throw new IllegalStateException("Transfer already exists for participant: " + participant);
        }
    }
}
