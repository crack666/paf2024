package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Standardized Data Transfer Object for Notification entity.
 * Used for all notification-related operations including WebSocket communication.
 */
@Setter
@Getter
@Schema(description = "Standardized Data Transfer Object for notifications")
public class NotificationDTO {

    // Getters and setters
    @Schema(description = "Unique identifier of the notification")
    private int id;

    @Schema(description = "Message content")
    private String message;

    @Schema(description = "Timestamp when the notification was created")
    private LocalDateTime timestamp;

    @Schema(description = "Whether the notification has been read")
    private boolean read;

    @Schema(description = "Notification urgency level (HIGH, NORMAL, LOW)")
    private String urgency;

    @Schema(description = "Type of notification (e.g. TASK_CREATED, TASK_COMPLETED, TASK_OVERDUE, DEADLOCK_DETECTED, SYSTEM)")
    private String type;

    @Schema(description = "Related task ID, if applicable")
    private Integer relatedTaskId;

    @Schema(description = "ID of the user this notification is for (0 for system notifications)")
    private int userId;

    @Schema(description = "Notification status (CREATED, SENT, DELIVERED, READ, ARCHIVED)")
    private String status;

    /**
     * Default no-args constructor for serialization
     */
    public NotificationDTO() {
    }

    /**
     * Create a DTO from a domain notification model
     */
    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.timestamp = notification.getTimestamp();
        this.read = notification.isRead();
        this.urgency = notification.getUrgency();
        this.type = notification.getType();
        this.relatedTaskId = notification.getRelatedTaskId();
        this.userId = notification.getUserId();
        this.status = notification.getStatus().toString();
    }

    /**
     * Factory method to create a NotificationDTO with all the details.
     */
    public static NotificationDTO of(int id, String message, LocalDateTime timestamp,
                                     boolean read, String urgency, String type,
                                     Integer relatedTaskId, int userId, String status) {
        NotificationDTO dto = new NotificationDTO();
        dto.id = id;
        dto.message = message;
        dto.timestamp = timestamp;
        dto.read = read;
        dto.urgency = urgency;
        dto.type = type;
        dto.relatedTaskId = relatedTaskId;
        dto.userId = userId;
        dto.status = status;
        return dto;
    }
}