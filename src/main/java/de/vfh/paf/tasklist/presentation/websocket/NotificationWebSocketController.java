package de.vfh.paf.tasklist.presentation.websocket;

import de.vfh.paf.tasklist.application.dto.NotificationDTO;
import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
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
     * Returns ALL notifications for the requested user (both read and unread).
     *
     * @param message The message containing userId
     * @return List of notifications
     */
    @MessageMapping("/notifications.subscribe")
    @SendToUser("/queue/notifications")
    public List<NotificationDTO> subscribeToNotifications(@Payload Map<String, Object> message) {
        Integer userId = (Integer) message.get("userId");
        if (userId == null) {
            return List.of();
        }

        // Get ALL notifications for the user (not just unread)
        List<Notification> notifications = notificationService.findByUserId(userId);

        // Return as DTOs directly (no more NotificationPayload)
        return notifications.stream()
                .map(NotificationDTO::new)
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
    public NotificationDTO broadcastNotification(@Payload Map<String, String> message) {
        String type = message.getOrDefault("type", "BROADCAST");
        String content = message.getOrDefault("message", "System broadcast message");
        String urgency = message.getOrDefault("urgency", "NORMAL");

        // Use the service to broadcast (this will send the WebSocket message too)
        notificationService.broadcastSystemNotification(type, content, urgency, null);

        // Create a DTO for the response
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage(content);
        dto.setType(type);
        dto.setUrgency(urgency);
        dto.setTimestamp(LocalDateTime.now());
        dto.setUserId(0); // System notification
        dto.setStatus("SENT");
        
        return dto;
    }
}