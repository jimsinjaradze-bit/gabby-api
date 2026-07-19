package io.cutehat.gabby.domain.discovery.handler;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import io.cutehat.gabby.domain.discovery.NodeService;
import io.cutehat.gabby.domain.discovery.RosterBroadcaster;
import io.cutehat.gabby.domain.dispatch.CommandContext;
import io.cutehat.gabby.domain.dispatch.ClientCommandHandler;
import org.springframework.stereotype.Service;

@Service
public class DeregisterHandlerClient implements ClientCommandHandler {
    private final NodeService nodeService;
    private final RosterBroadcaster rosterBroadcaster;

    public DeregisterHandlerClient(NodeService nodeService, RosterBroadcaster rosterBroadcaster) {
        this.nodeService = nodeService;
        this.rosterBroadcaster = rosterBroadcaster;
    }

    @Override
    public ClientCommandType type() {
        return ClientCommandType.DEREGISTER;
    }

    @Override
    public void handle(CommandContext ctx, ClientCommand cmd) {
        nodeService.deregister(ctx.senderName());
        rosterBroadcaster.broadcastRoster();
    }
}
