package io.cutehat.gabby.api.protocol;

public record ServerMessage(ServerMessageType messageType, Object payload) {
}
