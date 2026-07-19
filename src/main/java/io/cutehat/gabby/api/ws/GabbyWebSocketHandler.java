package io.cutehat.gabby.api.ws;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import io.cutehat.gabby.domain.dispatch.CommandContext;
import io.cutehat.gabby.domain.dispatch.TextCommandDispatchService;
import io.cutehat.gabby.domain.transfer.relay.PayloadRelayService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GabbyWebSocketHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(GabbyWebSocketHandler.class);

    private final TextCommandDispatchService textCommandDispatchService;
    private final PayloadRelayService payloadRelayService;

    public GabbyWebSocketHandler(TextCommandDispatchService textCommandDispatchService, PayloadRelayService payloadRelayService) {
        this.textCommandDispatchService = textCommandDispatchService;
        this.payloadRelayService = payloadRelayService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String name = extractNameFromUri(session);
        session.getAttributes().put(SessionAttributeNames.NAME.name(), name);

        log.info("Connection established: session={}, name={}", session.getId(), name);
        CommandContext ctx = new CommandContext(name, session);
        textCommandDispatchService.dispatch(ctx, new ClientCommand(ClientCommandType.REGISTER, null));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        String name = extractNameFromSession(session);

        if (message instanceof BinaryMessage binaryMessage) {
            payloadRelayService.relay(new CommandContext(name, session), binaryMessage);
            return;
        }

        if (!(message instanceof TextMessage textMessage)) {
            log.warn("Ignoring non-text message from name={}, session={}", name, session.getId());
            return;
        }

        CommandContext ctx = new CommandContext(name, session);
        textCommandDispatchService.dispatch(ctx, textMessage.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String name = extractNameFromSession(session);
        log.error("Transport error for name={}, session={}", name, session.getId(), exception);
        deregister(name, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String name = extractNameFromSession(session);
        log.info("Connection closed: name={}, session={}, status={}", name, session.getId(), closeStatus);
        deregister(name, session);
    }

    private void deregister(String name, WebSocketSession session) {
        CommandContext ctx = new CommandContext(name, session);
        textCommandDispatchService.dispatch(ctx, new ClientCommand(ClientCommandType.DEREGISTER, null));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private static String extractNameFromSession(WebSocketSession session) {
        return (String) session.getAttributes().get(SessionAttributeNames.NAME.name());
    }

    private static @NonNull String extractNameFromUri(WebSocketSession session) {
        String name = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams().getFirst("name");
        if (!StringUtils.hasLength(name)) {
            throw new IllegalStateException("You should provide name when connecting");
        }
        return name;
    }
}
