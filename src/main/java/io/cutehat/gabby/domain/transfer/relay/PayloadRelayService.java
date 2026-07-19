package io.cutehat.gabby.domain.transfer.relay;

import io.cutehat.gabby.domain.dispatch.CommandContext;
import io.cutehat.gabby.domain.messaging.NodeMessenger;
import io.cutehat.gabby.domain.transfer.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

@Service
public class PayloadRelayService {
    private static final Logger log = LoggerFactory.getLogger(PayloadRelayService.class);

    private final TransferRepository transferRepository;
    private final NodeMessenger messenger;
    private final TransferMulticaster transferMulticaster;

    public PayloadRelayService(TransferRepository repository, NodeMessenger messenger, TransferMulticaster transferMulticaster) {
        this.transferRepository = repository;
        this.messenger = messenger;
        this.transferMulticaster = transferMulticaster;
    }

    public void relay(CommandContext ctx, BinaryMessage message) {
        TransferEntity transfer = getTransfer(ctx);

        if (transfer.getStatus() == TransferStatus.ACCEPTED) {
            transfer.setStatus(TransferStatus.IN_PROGRESS);
        }

        try {
            messenger.sendBinary(transfer.getTo(), new BinaryMessage(message.getPayload()));
            transfer.getTransferMeta().addBytesStreamed(message.getPayloadLength());

            if (transfer.getTransferMeta().getBytesStreamed() >= transfer.getFileMeta().sizeInBytes()) {
                transfer.setStatus(TransferStatus.COMPLETED_SUCCESSFULLY);
                transferMulticaster.multicastToParticipants(transfer);
            }
        } catch (IOException e) {
            log.error("transfer with id: {} failed", transfer.getId(), e);
            transfer.setStatus(TransferStatus.COMPLETED_WITH_ERROR);
        }
    }

    private @NonNull TransferEntity getTransfer(CommandContext ctx) {
        TransferEntity transfer = transferRepository.getBySenderAndStatuses(
                ctx.senderName(), List.of(TransferStatus.IN_PROGRESS, TransferStatus.ACCEPTED)
        ).orElseThrow();
        return transfer;
    }

}
