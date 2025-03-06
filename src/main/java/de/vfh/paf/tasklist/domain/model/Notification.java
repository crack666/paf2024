package de.vfh.paf.tasklist.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a notification sent to a user in the system.
 */
public class Notification {
    private final int id;
    private final int userId;
    private final String message;
    private final String urgency;
    private final String type;
    private final Integer relatedTaskId;
    private LocalDateTime sentAt;
    private boolean isRead;

    /**
     * Creates a simple notification.
     *
     * @param id The unique identifier for the notification
     * @param userId The ID of the user to whom the notification is sent
     * @param message The notification message
     */
    public Notification(int id, int userId, String message) {
        this(id, message, "NORMAL", "INFO", userId, null);
    }
    
    /**
     * Creates a detailed notification.
     *
     * @param id The unique identifier for the notification
     * @param message The notification message
     * @param urgency The urgency level (HIGH, NORMAL, LOW)
     * @param userId The ID of the user to whom the notification is sent
     * @param relatedTaskId The ID of the related task, if any
     */
    public Notification(int id, String message, String urgency, int userId, Integer relatedTaskId) {
        this(id, message, urgency, "INFO", userId, relatedTaskId);
    }
    
    /**
     * Creates a fully detailed notification.
     *
     * @param id The unique identifier for the notification
     * @param message The notification message
     * @param urgency The urgency level (HIGH, NORMAL, LOW)
     * @param type The type of notification (e.g. TASK_CREATED, TASK_COMPLETED, TASK_OVERDUE)
     * @param userId The ID of the user to whom the notification is sent
     * @param relatedTaskId The ID of the related task, if any
     */
    public Notification(int id, String message, String urgency, String type, int userId, Integer relatedTaskId) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.urgency = urgency;
        this.type = type;
        this.relatedTaskId = relatedTaskId;
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    /**
     * Marks the notification as read.
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Sends the notification.
     * In a real implementation, this would involve some notification service.
     *
     * @return true if the notification was sent successfully
     */
    public boolean send() {
        // In a real application, this would send the notification via a notification service
        this.sentAt = LocalDateTime.now();
        return true;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public LocalDateTime getTimestamp() {
        return sentAt;
    }

    public boolean isRead() {
        return isRead;
    }
    
    public String getUrgency() {
        return urgency;
    }
    
    public String getType() {
        return type;
    }
    
    public Integer getRelatedTaskId() {
        return relatedTaskId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}