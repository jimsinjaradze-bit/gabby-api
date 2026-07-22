package io.cutehat.gabby.domain.policy;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import io.cutehat.gabby.api.protocol.payload.PolicyDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PolicyIntegrationTest {
    private WebSocketSession alice;
    @Value("${local.server.port}")
    int port;
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void closeSessions() throws Exception {
        if (alice != null && alice.isOpen()) alice.close();
    }

    @Test
    void testPolicyFetch() throws ExecutionException, InterruptedException, TimeoutException, IOException {
        var client = new StandardWebSocketClient();
        var wsHandler = new ServerResponseHandler();

        long TIMEOUT_MS = 5_000;
        alice = client.execute(wsHandler, wsUri("alice")).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);

        var command = new ClientCommand(ClientCommandType.GET_POLICY, null);;
        alice.sendMessage(new TextMessage(objectMapper.writeValueAsString(command)));

        await()
                .atMost(Duration.ofMillis(TIMEOUT_MS))
                .pollInterval(Duration.ofMillis(50))
                .until(() -> wsHandler.getPolicyDTO() != null);

        PolicyDTO dto = wsHandler.getPolicyDTO();
        assertNotNull(dto.maxChunkSizeInBytes());
    }


    private String wsUri(String name) {
        return "ws://localhost:" + port + "/ws?name=" + name;
    }
}
