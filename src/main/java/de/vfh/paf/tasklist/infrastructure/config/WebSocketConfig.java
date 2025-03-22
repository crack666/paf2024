package de.vfh.paf.tasklist.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration for WebSocket connections.
 * Enables STOMP (Simple Text Oriented Messaging Protocol) over WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker.
     * 
     * @param config The MessageBrokerRegistry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topics that clients can subscribe to, and receive messages from the server
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages that clients send to the server
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");
        
        // The /topic/tasks will be used for task status updates
        // The /topic/queues will be used for queue-related updates
    }

    /**
     * Registers endpoints for WebSocket connections.
     * 
     * @param registry The StompEndpointRegistry to configure
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint for WebSocket connections
        registry.addEndpoint("/ws")
                // Allow request from any origin (CORS configuration)
                .setAllowedOriginPatterns("*")
                // Enable SockJS fallback options for browsers that don't support WebSocket
                .withSockJS();
    }
}