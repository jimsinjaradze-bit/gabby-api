package io.cutehat.gabby.domain.dispatch;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;

public interface ClientCommandHandler {
    ClientCommandType type();

    void handle(CommandContext ctx, ClientCommand cmd);

    default int order() {
        return 1;
    }
}
