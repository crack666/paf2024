package de.vfh.paf.tasklist.application.dto;

import java.time.LocalDateTime;

/**
 * Payload for WebSocket notifications.
 * Contains all the necessary information for a notification to be sent via WebSocket.
 */
public class NotificationPayload {
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
    
    // Getters and Setters
    public int getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getUrgency() {
        return urgency;
    }
    
    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }
    
    public Integer getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
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
}