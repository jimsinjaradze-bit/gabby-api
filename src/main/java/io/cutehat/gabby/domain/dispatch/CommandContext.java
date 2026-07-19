package io.cutehat.gabby.domain.dispatch;

import org.springframework.web.socket.WebSocketSession;

public record CommandContext(String senderName, WebSocketSession session) {
}
