package io.cutehat.gabby.domain.transfer;

import io.cutehat.gabby.api.protocol.ServerMessage;
import io.cutehat.gabby.api.protocol.ServerMessageType;
import io.cutehat.gabby.api.protocol.payload.NodeDTO;
import io.cutehat.gabby.api.protocol.payload.TransferDTO;
import io.cutehat.gabby.domain.discovery.NodeService;
import io.cutehat.gabby.domain.messaging.NodeMessenger;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TransferMulticaster {
    private static final Logger log = LoggerFactory.getLogger(TransferMulticaster.class);

    private final TransferService transferService;
    private final NodeMessenger messenger;

    public TransferMulticaster(TransferService transferService, NodeMessenger messenger) {
        this.transferService = transferService;
        this.messenger = messenger;
    }

    public void multicastToParticipants(TransferEntity transfer) {
        try {
            messenger.send(transfer.getFrom(), new ServerMessage(ServerMessageType.TRANSFER_LIST, prepareTransfersFor(transfer.getFrom())));
            messenger.send(transfer.getTo(), new ServerMessage(ServerMessageType.TRANSFER_LIST, prepareTransfersFor(transfer.getTo())));
        } catch (IOException e) {
            log.error("Could not send transfer list", e);
            throw new RuntimeException(e);
        }
    }

    public void updateParticipant(String participant){
        try {
            messenger.send(participant, new ServerMessage(ServerMessageType.TRANSFER_LIST, prepareTransfersFor(participant)));
        } catch (IOException e) {
            log.error("Could not send transfer list", e);
            throw new RuntimeException(e);
        }
    }

    private @NonNull List<TransferDTO> prepareTransfersFor(String transfer) {
        return transferService.getAllByParticipant(transfer)
                .stream().map(TransferDTO::from)
                .toList();
    }
}
