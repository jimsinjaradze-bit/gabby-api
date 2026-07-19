package io.cutehat.gabby.domain.discovery;

import io.cutehat.gabby.domain.transfer.config.TransferPolicyProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.util.Collection;

@Service
public class NodeService {
    private final NodeRepository nodeRepository;
    private final int SEND_TIME_LIMIT = 2000;
    private final TransferPolicyProperties transferPolicyProperties;

    public NodeService(NodeRepository nodeRepository, TransferPolicyProperties transferPolicyProperties) {
        this.nodeRepository = nodeRepository;
        this.transferPolicyProperties = transferPolicyProperties;
    }

    public void register(String name, WebSocketSession session) {
        nodeRepository.register(name, new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, transferPolicyProperties.maxChunkSizeInBytes() * 5));
    }

    public void deregister(String name) {
        nodeRepository.remove(name);
    }

    public Node fetchOrThrow(String name) {
        return nodeRepository.fetchOrThrow(name);
    }

    public Collection<Node> fetchAll() {
        return nodeRepository.fetchAll();
    }
}
