package io.cutehat.gabby.domain.dispatch;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class TextCommandDispatchService {
    private static final Logger log = LoggerFactory.getLogger(TextCommandDispatchService.class);

    private final Map<ClientCommandType, List<ClientCommandHandler>> handlers;
    private final ObjectMapper objectMapper;

    public TextCommandDispatchService(List<ClientCommandHandler> handlers,
                                      ObjectMapper objectMapper) {
        this.handlers = handlers.stream()
                .collect(Collectors.groupingBy(ClientCommandHandler::type));
        this.objectMapper = objectMapper;
    }

    public void dispatch(CommandContext ctx, String rawMessage) {
        dispatch(ctx, objectMapper.readValue(rawMessage, ClientCommand.class));
    }

    public void dispatch(CommandContext ctx, ClientCommand command) {
        List<ClientCommandHandler> matched = handlers.get(command.clientCommandType());
        if (matched == null || matched.isEmpty()) {
            throw new IllegalArgumentException("No handler for command: " + command.clientCommandType());
        }

        log.debug("Dispatching {} from {}", command.clientCommandType(), ctx.senderName());
        matched
                .stream()
                .sorted(Comparator.comparing(ClientCommandHandler::order))
                .forEach(handler -> handler.handle(ctx, command));
    }
}
