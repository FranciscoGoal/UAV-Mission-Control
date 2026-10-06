package com.example.missioncontrol.infrastructure.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WebSocketConfigurationTest {

    private final WebSocketConfiguration configuration = new WebSocketConfiguration();

    @Test
    void configuresMessageBrokerPrefixes() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        configuration.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).setUserDestinationPrefix("/user");
    }

    @Test
    void registersWebSocketEndpoint() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);

        configuration.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws");
    }
}
