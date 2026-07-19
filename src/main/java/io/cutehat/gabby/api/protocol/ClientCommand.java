package io.cutehat.gabby.api.protocol;

import tools.jackson.databind.JsonNode;

public record ClientCommand(ClientCommandType clientCommandType, JsonNode payload) {
}