package io.cutehat.gabby.domain.discovery;

import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;

public record Node(String name, WebSocketSession session, Instant joinedAt) {
}
