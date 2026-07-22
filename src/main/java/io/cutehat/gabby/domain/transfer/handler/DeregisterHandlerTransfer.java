package io.cutehat.gabby.domain.transfer.handler;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import io.cutehat.gabby.domain.dispatch.ClientCommandHandler;
import io.cutehat.gabby.domain.dispatch.CommandContext;
import io.cutehat.gabby.domain.transfer.TransferMulticaster;
import io.cutehat.gabby.domain.transfer.TransferService;
import org.springframework.stereotype.Service;

@Service
public class DeregisterHandlerTransfer implements ClientCommandHandler {
    private final TransferService transferService;
    private final TransferMulticaster transferMulticaster;

    public DeregisterHandlerTransfer(TransferService transferService, TransferMulticaster transferMulticaster) {
        this.transferService = transferService;
        this.transferMulticaster = transferMulticaster;
    }

    @Override
    public ClientCommandType type() {
        return ClientCommandType.DEREGISTER;
    }

    @Override
    public void handle(CommandContext ctx, ClientCommand cmd) {
        String disconnected = ctx.senderName();
        var affected = transferService.cleanForParticipant(disconnected);
        affected.forEach(transfer -> {
            var remainingParticipant = transfer.getFrom().equalsIgnoreCase(disconnected) ? transfer.getTo() : transfer.getFrom();
            transferMulticaster.updateParticipant(remainingParticipant);
        });
    }

    @Override
    public int order() {
        return 2;
    }
}