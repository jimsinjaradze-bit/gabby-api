package io.cutehat.gabby.domain.transfer.handler;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import io.cutehat.gabby.api.protocol.ServerMessage;
import io.cutehat.gabby.api.protocol.ServerMessageType;
import io.cutehat.gabby.api.protocol.payload.RejectTransferReq;
import io.cutehat.gabby.api.protocol.payload.TransferDTO;
import io.cutehat.gabby.domain.dispatch.ClientCommandHandler;
import io.cutehat.gabby.domain.dispatch.CommandContext;
import io.cutehat.gabby.domain.messaging.NodeMessenger;
import io.cutehat.gabby.domain.transfer.TransferMulticaster;
import io.cutehat.gabby.domain.transfer.TransferService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Service
public class TransferRejectHandler implements ClientCommandHandler {
    private final ObjectMapper objectMapper;
    private final TransferService transferService;
    private final TransferMulticaster multicaster;

    public TransferRejectHandler(ObjectMapper objectMapper, TransferService transferService, TransferMulticaster multicaster) {
        this.objectMapper = objectMapper;
        this.transferService = transferService;
        this.multicaster = multicaster;
    }

    @Override
    public ClientCommandType type() {
        return ClientCommandType.REJECT_PAYLOAD_REQ;
    }

    @Override
    public void handle(CommandContext ctx, ClientCommand cmd) {
        var sender = ctx.senderName();
        var parsedCmd = objectMapper.treeToValue(cmd.payload(), RejectTransferReq.class);

        var transfer = transferService.reject(parsedCmd.transferId(), sender);
        multicaster.multicastToParticipants(transfer);
    }
}
