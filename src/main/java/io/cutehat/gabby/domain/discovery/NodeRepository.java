package io.cutehat.gabby.domain.discovery;

import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class NodeRepository {
    private final Integer MAX_CAPACITY = 4;
    private final Map<String, Node> nodes = new ConcurrentHashMap<>(MAX_CAPACITY, 1f);

    public void register(String name, WebSocketSession session) {
        if (nodes.size() >= MAX_CAPACITY) {
            throw new IllegalStateException("Not accepting any more connections");
        }
        nodes.put(name, new Node(name, session, Instant.now()));
    }

    public Collection<Node> fetchAll() {
        return nodes.values();
    }

    public Optional<Node> fetch(String name) {
        return Optional.ofNullable(nodes.get(name));
    }

    public Node fetchOrThrow(String name) {
        return fetch(name).orElseThrow();
    }

    public void remove(String name) {
        nodes.remove(name);
    }

    public boolean exists(String name){
        return nodes.containsKey(name);
    }
}
