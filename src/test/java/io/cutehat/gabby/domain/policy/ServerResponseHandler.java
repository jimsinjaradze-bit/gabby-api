package io.cutehat.gabby.domain.policy;

import io.cutehat.gabby.api.protocol.ServerMessageType;
import io.cutehat.gabby.api.protocol.payload.PolicyDTO;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class ServerResponseHandler extends AbstractWebSocketHandler {
    private ObjectMapper objectMapper = new ObjectMapper();
    private PolicyDTO policyDTO;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode node = objectMapper.readTree(message.getPayload());

        var rawMessageType = node.get("messageType").asString();
        var rawPayload = node.get("payload");
        var messageType = ServerMessageType.valueOf(rawMessageType);

        if (messageType == ServerMessageType.POLICY) {
            policyDTO = objectMapper.treeToValue(rawPayload, PolicyDTO.class);
        }
    }

    public PolicyDTO getPolicyDTO() {
        return policyDTO;
    }
}
