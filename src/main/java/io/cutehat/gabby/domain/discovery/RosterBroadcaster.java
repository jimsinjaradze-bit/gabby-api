package io.cutehat.gabby.domain.discovery;

import io.cutehat.gabby.api.protocol.ServerMessage;
import io.cutehat.gabby.api.protocol.ServerMessageType;
import io.cutehat.gabby.api.protocol.payload.NodeDTO;
import io.cutehat.gabby.domain.messaging.NodeMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RosterBroadcaster {
    private static final Logger log = LoggerFactory.getLogger(RosterBroadcaster.class);

    private final NodeService nodeService;
    private final NodeMessenger messenger;

    public RosterBroadcaster(NodeService nodeService, NodeMessenger messenger) {
        this.nodeService = nodeService;
        this.messenger = messenger;
    }

    public synchronized void broadcastRoster() {
        var allNodes = nodeService.fetchAll();
        var allNodeDTOs = allNodes.stream()
                .map(node -> new NodeDTO(node.name(), node.joinedAt()))
                .toList();

        var message = new ServerMessage(ServerMessageType.NODE_LIST, allNodeDTOs);
        allNodes.forEach(node -> {
            try {
                messenger.send(node.session(), message);
            } catch (IOException e) {
                log.error("Could not broadcast {} to {}", ServerMessageType.NODE_LIST, node.name(), e);
            }
        });
    }
}
