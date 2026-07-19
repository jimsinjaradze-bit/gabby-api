package io.cutehat.gabby.domain.messaging;

import io.cutehat.gabby.api.protocol.ServerMessage;
import io.cutehat.gabby.domain.discovery.Node;
import io.cutehat.gabby.domain.discovery.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class NodeMessenger {
    private static final Logger log = LoggerFactory.getLogger(NodeMessenger.class);

    private final NodeRepository nodeRepository;
    private final ObjectMapper objectMapper;

    public NodeMessenger(NodeRepository nodeRepository, ObjectMapper objectMapper) {
        this.nodeRepository = nodeRepository;
        this.objectMapper = objectMapper;
    }

    public void send(String to, ServerMessage frame) throws IOException {
        Node node = nodeRepository.fetchOrThrow(to);
        send(node.session(), frame);
    }

    public void send(WebSocketSession session, ServerMessage frame) throws IOException {
        send(session, objectMapper.writeValueAsString(frame));
    }

    public void send(WebSocketSession session, String payload) throws IOException {
        session.sendMessage(new TextMessage(payload));
    }

    public void sendBinary(String to, BinaryMessage message) throws IOException {
        Node node = nodeRepository.fetchOrThrow(to);
        node.session().sendMessage(message);
    }
}
