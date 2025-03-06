package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Notification entity.
 */
@Schema(description = "Data Transfer Object for notifications")
public class NotificationDTO {
    
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
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public String getUrgency() {
        return urgency;
    }
    
    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getRelatedTaskId() {
        return relatedTaskId;
    }
    
    public void setRelatedTaskId(Integer relatedTaskId) {
        this.relatedTaskId = relatedTaskId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
}