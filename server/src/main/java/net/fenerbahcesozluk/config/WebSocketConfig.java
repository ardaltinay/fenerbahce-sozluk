package net.fenerbahcesozluk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for broadcasting to subscribers
        // /topic for public broadcasts, /queue for private messages
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages from clients to server
        config.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint that clients will connect to
        // Use CORS origins from application.yml
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }
}
