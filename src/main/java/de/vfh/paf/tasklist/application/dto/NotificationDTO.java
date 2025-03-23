package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Notification entity.
 */
@Setter
@Getter
@Schema(description = "Data Transfer Object for notifications")
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

    @Schema(description = "Notification urgency level")
    private String urgency;

    @Schema(description = "Type of notification (e.g. TASK_CREATED, TASK_COMPLETED, TASK_OVERDUE)")
    private String type;

    @Schema(description = "Related task ID, if applicable")
    private Integer relatedTaskId;

    @Schema(description = "ID of the user this notification is for")
    private int userId;

    public NotificationDTO() {
    }

    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.timestamp = notification.getTimestamp();
        this.read = notification.isRead();
        this.urgency = notification.getUrgency();
        this.type = notification.getType();
        this.relatedTaskId = notification.getRelatedTaskId();
        this.userId = notification.getUserId();
    }

}