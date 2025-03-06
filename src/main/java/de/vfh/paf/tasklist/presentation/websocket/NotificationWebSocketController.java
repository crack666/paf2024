package de.vfh.paf.tasklist.presentation.websocket;

import de.vfh.paf.tasklist.application.dto.NotificationDTO;
import de.vfh.paf.tasklist.application.dto.NotificationPayload;
import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebSocket controller for handling real-time notifications.
 */
@Controller
public class NotificationWebSocketController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationWebSocketController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Handles subscription to notifications from clients.
     * Returns the list of unread notifications for the requested user.
     * 
     * @param userId The user ID
     * @param headerAccessor The message headers
     * @return List of notification payloads
     */
    @MessageMapping("/notifications.subscribe")
    @SendToUser("/queue/notifications")
    public List<NotificationPayload> subscribeToNotifications(@Payload Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        Integer userId = (Integer) message.get("userId");
        if (userId == null) {
            return List.of();
        }
        
        // Get unread notifications for the user
        List<Notification> notifications = notificationService.findByUserIdAndReadStatus(userId, false);
        
        // Return as payload
        return notifications.stream()
                .map(NotificationDTO::new)
                .map(NotificationPayload::fromDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Marks a notification as read.
     * 
     * @param message The message containing notificationId and userId
     * @return Acknowledgment message
     */
    @MessageMapping("/notifications.markRead")
    @SendToUser("/queue/notifications.ack")
    public Map<String, Object> markNotificationAsRead(@Payload Map<String, Object> message) {
        Integer notificationId = (Integer) message.get("notificationId");
        Integer userId = (Integer) message.get("userId");
        
        if (notificationId == null || userId == null) {
            return Map.of("success", false, "message", "Missing notification ID or user ID");
        }
        
        Notification notification = notificationService.markAsRead(userId, notificationId);
        boolean success = notification != null;
        
        return Map.of(
            "success", success,
            "notificationId", notificationId,
            "message", success ? "Notification marked as read" : "Notification not found or unauthorized"
        );
    }
    
    /**
     * Broadcasts a notification to all connected clients.
     * This method is primarily for testing purposes.
     * 
     * @param message The message containing the notification details
     * @return The broadcast notification
     */
    @MessageMapping("/notifications.broadcast")
    @SendTo("/topic/notifications")
    public NotificationPayload broadcastNotification(@Payload Map<String, String> message) {
        String type = message.getOrDefault("type", "BROADCAST");
        String content = message.getOrDefault("message", "System broadcast message");
        String urgency = message.getOrDefault("urgency", "NORMAL");
        
        // Use the service to broadcast (this will send the WebSocket message too)
        notificationService.broadcastSystemNotification(type, content, urgency);
        
        // Create a simple payload for the response
        NotificationPayload payload = new NotificationPayload();
        payload.setMessage(content);
        payload.setType(type);
        payload.setUrgency(urgency);
        payload.setTimestamp(java.time.LocalDateTime.now());
        
        return payload;
    }
}