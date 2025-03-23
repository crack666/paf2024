package de.vfh.paf.tasklist.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Payload for WebSocket notifications.
 * Contains all the necessary information for a notification to be sent via WebSocket.
 */
@Setter
@Getter
public class NotificationPayload {
    // Getters and Setters
    private int notificationId;
    private String type;
    private String message;
    private String urgency;
    private Integer taskId;
    private LocalDateTime timestamp;
    private boolean read;

    /**
     * Default constructor required by JSON serialization
     */
    public NotificationPayload() {
    }

    /**
     * Full constructor for the notification payload
     */
    public NotificationPayload(int notificationId, String type, String message, String urgency,
                               Integer taskId, LocalDateTime timestamp, boolean read) {
        this.notificationId = notificationId;
        this.type = type;
        this.message = message;
        this.urgency = urgency;
        this.taskId = taskId;
        this.timestamp = timestamp;
        this.read = read;
    }

    /**
     * Factory method to create a payload from a DTO.
     *
     * @param dto The NotificationDTO to convert
     * @return The NotificationPayload
     */
    public static NotificationPayload fromDto(NotificationDTO dto) {
        return new NotificationPayload(
                dto.getId(),
                dto.getType(),
                dto.getMessage(),
                dto.getUrgency(),
                dto.getRelatedTaskId(),
                dto.getTimestamp(),
                dto.isRead()
        );
    }

}