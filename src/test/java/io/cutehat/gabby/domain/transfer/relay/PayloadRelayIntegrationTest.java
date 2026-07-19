package io.cutehat.gabby.domain.transfer.relay;

import io.cutehat.gabby.api.protocol.ClientCommand;
import io.cutehat.gabby.api.protocol.ClientCommandType;
import io.cutehat.gabby.api.protocol.payload.FileMeta;
import io.cutehat.gabby.api.protocol.payload.RejectTransferReq;
import io.cutehat.gabby.api.protocol.payload.SendPayloadReq;
import io.cutehat.gabby.domain.transfer.TransferStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PayloadRelayIntegrationTest {
    private static final long TIMEOUT_MS = 10_000;
    private static final int FILE_SIZE = 64 * 1024;
    private static final int CHUNK_SIZE = 4 * 1024; // below the 8KB default websocket buffer

    @Value("${local.server.port}")
    int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebSocketSession alice;
    private WebSocketSession bob;

    @AfterEach
    void closeSessions() throws Exception {
        if (alice != null && alice.isOpen()) alice.close();
        if (bob != null && bob.isOpen()) bob.close();
    }

    @Test
    void testHappyPath() throws Exception {
        // Given
        var client = new StandardWebSocketClient();
        var handlerForAlice = new ServerResponseHandler(FILE_SIZE);
        var handlerForBob = new ServerResponseHandler(FILE_SIZE);

        alice = client.execute(handlerForAlice, wsUri("alice")).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        bob = client.execute(handlerForBob, wsUri("bob")).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);

        awaitUntilNodesAreConnected(handlerForAlice, TIMEOUT_MS);
        awaitUntilNodesAreConnected(handlerForBob, TIMEOUT_MS);

        // When: alice requests to send a payload to bob
        var fileMeta = new FileMeta("test.txt", FILE_SIZE, "application/octet-stream");
        var sendPayloadReq = new ClientCommand(ClientCommandType.SEND_PAYLOAD_REQ,
                objectMapper.valueToTree(new SendPayloadReq("bob", fileMeta)));
        alice.sendMessage(new TextMessage(objectMapper.writeValueAsString(sendPayloadReq)));

        String transferId = awaitUntilTransferArrives(handlerForBob, TIMEOUT_MS);

        // And: bob accepts it
        var acceptReq = new ClientCommand(ClientCommandType.ACCEPT_PAYLOAD_REQ,
                objectMapper.valueToTree(new RejectTransferReq(transferId)));
        bob.sendMessage(new TextMessage(objectMapper.writeValueAsString(acceptReq)));

        awaitUntilTransferHasStatus(handlerForAlice, transferId, TransferStatus.ACCEPTED, TIMEOUT_MS);

        // And: alice streams the file in chunks
        byte[] file = new byte[FILE_SIZE];
        new Random(42).nextBytes(file);

        for (int offset = 0; offset < file.length; offset += CHUNK_SIZE) {
            alice.sendMessage(new BinaryMessage(Arrays.copyOfRange(file, offset, Math.min(offset + CHUNK_SIZE, file.length))));
        }

        // Then: bob receives exactly what was sent and both sides see the transfer complete
        awaitUntilAllBytesReceived(handlerForBob, FILE_SIZE, TIMEOUT_MS);
        assertArrayEquals(file, handlerForBob.getData());

        awaitUntilTransferHasStatus(handlerForAlice, transferId, TransferStatus.COMPLETED_SUCCESSFULLY, TIMEOUT_MS);
        awaitUntilTransferHasStatus(handlerForBob, transferId, TransferStatus.COMPLETED_SUCCESSFULLY, TIMEOUT_MS);
    }

    public void awaitUntilAllBytesReceived(ServerResponseHandler handler, int expectedBytes, long timeout) {
        long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline) {
            if (handler.getBytesReceived() == expectedBytes) {
                return;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new AssertionError("expected " + expectedBytes + " bytes but received " + handler.getBytesReceived());
    }

    public String awaitUntilTransferArrives(ServerResponseHandler handler, long timeout) {
        long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline) {
            if (!handler.getTransfers().isEmpty()) {
                return handler.getTransfers().get(0).id();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new AssertionError("transfer request did not arrive");
    }

    public void awaitUntilTransferHasStatus(ServerResponseHandler handler, String transferId, TransferStatus status, long timeout) {
        long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline) {
            boolean matches = handler.getTransfers().stream()
                    .anyMatch(transfer -> transfer.id().equals(transferId) && transfer.status() == status);
            if (matches) {
                return;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new AssertionError("transfer " + transferId + " did not reach status " + status);
    }

    public void awaitUntilNodesAreConnected(ServerResponseHandler handler, long timeout) {
        long deadline = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < deadline) {
            if (handler.getConnectedNodes().size() == 2){
                return;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new AssertionError("nodes did not connect");
    }

    private String wsUri(String name) {
        return "ws://localhost:" + port + "/ws?name=" + name;
    }
}
