package io.cutehat.gabby.domain.transfer.relay;

import io.cutehat.gabby.api.protocol.ServerMessageType;
import io.cutehat.gabby.api.protocol.payload.NodeDTO;
import io.cutehat.gabby.api.protocol.payload.TransferDTO;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ServerResponseHandler extends AbstractWebSocketHandler {
    private volatile List<NodeDTO> connectedNodes = new ArrayList<>();
    private volatile List<TransferDTO> transfers = new ArrayList<>();
    private volatile byte[] data;
    private volatile AtomicInteger bytesReceived = new AtomicInteger(0);
    private ObjectMapper objectMapper = new ObjectMapper();

    public ServerResponseHandler(int bufferSize) {
        data = new byte[bufferSize];
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        JsonNode node = objectMapper.readTree(payload);

        var rawMessageType = node.get("messageType").asString();
        var rawPayload = node.get("payload");
        var messageType = ServerMessageType.valueOf(rawMessageType);

        if (messageType == ServerMessageType.NODE_LIST) {
            connectedNodes = objectMapper.treeToValue(rawPayload,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, NodeDTO.class));
        } else if (messageType == ServerMessageType.TRANSFER_LIST) {
            transfers = objectMapper.treeToValue(rawPayload,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, TransferDTO.class));
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        int length = message.getPayloadLength();
        message.getPayload().get(data, bytesReceived.get(), length);
        bytesReceived.addAndGet(length);
    }

    public byte[] getData() {
        return data;
    }

    public int getBytesReceived() {
        return bytesReceived.get();
    }

    public List<NodeDTO> getConnectedNodes() {
        return connectedNodes;
    }

    public List<TransferDTO> getTransfers() {
        return transfers;
    }
}