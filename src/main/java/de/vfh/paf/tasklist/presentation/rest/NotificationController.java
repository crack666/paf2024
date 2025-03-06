package de.vfh.paf.tasklist.presentation.rest;

import de.vfh.paf.tasklist.application.dto.NotificationDTO;
import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for notification operations.
 */
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @GetMapping
    @Operation(summary = "Get all notifications", description = "Retrieves all notifications in the system")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        List<Notification> notifications = notificationService.findAll();
        List<NotificationDTO> dtos = notifications.stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieves a notification by its ID")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable int id) {
        Optional<Notification> notification = notificationService.findById(id);
        return notification.map(n -> ResponseEntity.ok(new NotificationDTO(n)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications for user", description = "Retrieves all notifications for a specific user")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(
            @PathVariable int userId,
            @RequestParam(required = false) Boolean read) {
        
        List<Notification> notifications;
        if (read != null) {
            notifications = notificationService.findByUserIdAndReadStatus(userId, read);
        } else {
            notifications = notificationService.findByUserId(userId);
        }
        
        List<NotificationDTO> dtos = notifications.stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a notification as read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable int id,
            @RequestBody Map<String, Integer> request) {
        
        Integer userId = request.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Notification notification = notificationService.markAsRead(userId, id);
        if (notification == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new NotificationDTO(notification));
    }
    
    @PostMapping("/broadcast")
    @Operation(summary = "Broadcast system notification", description = "Broadcasts a notification to all users")
    public ResponseEntity<Void> broadcastNotification(@RequestBody Map<String, String> request) {
        String type = request.getOrDefault("type", "SYSTEM");
        String message = request.get("message");
        String urgency = request.getOrDefault("urgency", "NORMAL");
        
        if (message == null || message.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean sent = notificationService.broadcastSystemNotification(type, message, urgency);
        return sent ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    @PostMapping("/send")
    @Operation(summary = "Send notification to user", description = "Sends a notification to a specific user")
    public ResponseEntity<NotificationDTO> sendNotification(@RequestBody Map<String, Object> request) {
        String type = (String) request.getOrDefault("type", "NOTIFICATION");
        String message = (String) request.get("message");
        String urgency = (String) request.getOrDefault("urgency", "NORMAL");
        Integer userId = (Integer) request.get("userId");
        Integer taskId = (Integer) request.get("taskId");
        
        if (message == null || userId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean sent = notificationService.sendNotification(type, urgency, userId, message, taskId);
        if (sent) {
            // Find the latest notification for this user
            List<Notification> notifications = notificationService.findByUserId(userId);
            if (!notifications.isEmpty()) {
                Notification latest = notifications.get(notifications.size() - 1);
                return ResponseEntity.ok(new NotificationDTO(latest));
            }
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}