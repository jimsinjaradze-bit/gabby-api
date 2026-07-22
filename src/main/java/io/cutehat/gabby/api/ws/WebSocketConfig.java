package io.cutehat.gabby.api.ws;

import io.cutehat.gabby.domain.policy.config.TransferPolicyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private static final String EXTENSIONS_HEADER = "Sec-WebSocket-Extensions";

    private final TransferPolicyProperties transferPolicyProperties;
    private final GabbyWebSocketHandler gabbyHandler;

    public WebSocketConfig(TransferPolicyProperties transferPolicyProperties, GabbyWebSocketHandler gabbyHandler) {
        this.transferPolicyProperties = transferPolicyProperties;
        this.gabbyHandler = gabbyHandler;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(gabbyHandler, "/ws")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxBinaryMessageBufferSize(transferPolicyProperties.maxChunkSizeInBytes());
        return container;
    }

    /**
     * Tomcat's WebSocket upgrade always re-reads Sec-WebSocket-Extensions straight off the
     * raw request and unconditionally offers permessage-deflate as "installed", regardless of
     * what Spring's HandshakeHandler negotiates. WebKit (all iOS browsers) closes with 1002 on
     * frames it disagrees with once that extension is active, so the only reliable way to keep
     * it off is to make sure Tomcat never sees the header requested in the first place.
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> stripWebSocketExtensionsFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {
                chain.doFilter(new ExtensionsStrippingRequest(request), response);
            }
        };

        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/ws");
        return registration;
    }

    private static class ExtensionsStrippingRequest extends HttpServletRequestWrapper {
        ExtensionsStrippingRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            return EXTENSIONS_HEADER.equalsIgnoreCase(name) ? null : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            return EXTENSIONS_HEADER.equalsIgnoreCase(name) ? Collections.emptyEnumeration() : super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(Collections.list(super.getHeaderNames()).stream()
                    .filter(name -> !EXTENSIONS_HEADER.equalsIgnoreCase(name))
                    .toList());
        }
    }
}
