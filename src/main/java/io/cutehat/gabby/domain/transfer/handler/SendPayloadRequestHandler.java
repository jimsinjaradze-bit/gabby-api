package io.cutehat.gabby.domain.transfer.handler;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import io.cutehat.gabby.api.protocol.ServerMessage;
import io.cutehat.gabby.api.protocol.ServerMessageType;
import io.cutehat.gabby.api.protocol.payload.SendPayloadReq;
import io.cutehat.gabby.api.protocol.payload.TransferDTO;
import io.cutehat.gabby.domain.dispatch.ClientCommandHandler;
import io.cutehat.gabby.domain.dispatch.CommandContext;
import io.cutehat.gabby.domain.messaging.NodeMessenger;
import io.cutehat.gabby.domain.transfer.TransferEntity;
import io.cutehat.gabby.domain.transfer.TransferMulticaster;
import io.cutehat.gabby.domain.transfer.TransferService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class SendPayloadRequestHandler implements ClientCommandHandler {
    private static final Logger log = LoggerFactory.getLogger(SendPayloadRequestHandler.class);

    private final TransferService transferService;
    private final ObjectMapper objectMapper;
    private final TransferMulticaster transferMulticaster;

    public SendPayloadRequestHandler(TransferService transferService,
                                     ObjectMapper objectMapper,
                                     NodeMessenger nodeMessenger,
                                     TransferMulticaster transferMulticaster) {
        this.transferService = transferService;
        this.objectMapper = objectMapper;
        this.transferMulticaster = transferMulticaster;
    }

    @Override
    public ClientCommandType type() {
        return ClientCommandType.SEND_PAYLOAD_REQ;
    }

    @Override
    public void handle(CommandContext ctx, ClientCommand cmd) {
        var parsedRequest = objectMapper.treeToValue(cmd.payload(), SendPayloadReq.class);
        TransferEntity transfer = transferService.requestTransfer(ctx.senderName(), parsedRequest.to(), parsedRequest.fileMeta());
        transferMulticaster.multicastToParticipants(transfer);
    }

}
