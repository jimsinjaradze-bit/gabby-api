package io.cutehat.gabby.api.ws;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.List;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final GabbyWebSocketHandler gabbyHandler;

    public WebSocketConfig(GabbyWebSocketHandler gabbyHandler) {
        this.gabbyHandler = gabbyHandler;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(gabbyHandler, "/ws")
                .setAllowedOrigins("*")
                .setHandshakeHandler(noExtensionsHandshakeHandler());
    }

    /**
     * Declines permessage-deflate (and any other extension). The payloads we
     * relay are already-compressed file bytes, so deflate only adds CPU and
     * wire overhead, and WebKit's implementation (all iOS browsers) closes
     * with 1002 on frames it disagrees with.
     */
    private static DefaultHandshakeHandler noExtensionsHandshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected @NonNull List<WebSocketExtension> filterRequestedExtensions(
                    @NonNull ServerHttpRequest request,
                    @NonNull List<WebSocketExtension> requestedExtensions,
                    @NonNull List<WebSocketExtension> supportedExtensions) {
                return List.of();
            }
        };
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxBinaryMessageBufferSize(65536);
        return container;
    }
}
