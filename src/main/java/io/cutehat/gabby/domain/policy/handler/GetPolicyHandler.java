package io.cutehat.gabby.domain.policy.handler;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import io.cutehat.gabby.api.protocol.ServerMessage;
import io.cutehat.gabby.api.protocol.ServerMessageType;
import io.cutehat.gabby.api.protocol.payload.PolicyDTO;
import io.cutehat.gabby.domain.dispatch.ClientCommandHandler;
import io.cutehat.gabby.domain.dispatch.CommandContext;
import io.cutehat.gabby.domain.messaging.NodeMessenger;
import io.cutehat.gabby.domain.policy.config.TransferPolicyProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GetPolicyHandler implements ClientCommandHandler {
    private final TransferPolicyProperties policyProperties;
    private final NodeMessenger messenger;

    public GetPolicyHandler(TransferPolicyProperties policyProperties, NodeMessenger messenger) {
        this.policyProperties = policyProperties;
        this.messenger = messenger;
    }

    @Override
    public ClientCommandType type() {
        return ClientCommandType.GET_POLICY;
    }

    @Override
    public void handle(CommandContext ctx, ClientCommand cmd) {
        var dto = PolicyDTO.from(policyProperties);
        try {
            messenger.send(ctx.session(), new ServerMessage(ServerMessageType.POLICY, dto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
