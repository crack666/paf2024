package de.vfh.paf.tasklist.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a notification sent to a user in the system.
 * Implements the State Pattern for managing notification lifecycle.
 */
@Setter
@Getter
@Entity
@Table(name = "notifications")
public class  Notification {
    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(length = 1000)
    private String message;
    
    private String urgency;
    
    private String type;
    
    @Column(name = "related_task_id")
    private Integer relatedTaskId;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    /**
     * Default constructor required by JPA
     */
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.status = NotificationStatus.CREATED;
    }

    /**
     * Creates a simple notification.
     *
     * @param id The unique identifier for the notification
     * @param userId The ID of the user to whom the notification is sent
     * @param message The notification message
     */
    public Notification(Integer id, Integer userId, String message) {
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
    public Notification(Integer id, String message, String urgency, Integer userId, Integer relatedTaskId) {
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
    public Notification(Integer id, String message, String urgency, String type, Integer userId, Integer relatedTaskId) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.urgency = urgency;
        this.type = type;
        this.relatedTaskId = relatedTaskId;
        this.createdAt = LocalDateTime.now();
        this.status = NotificationStatus.CREATED;
    }

    /**
     * Transitions the notification to the next state.
     * 
     * @param newStatus The new status to transition to
     * @return true if the transition is successful, false otherwise
     */
    public boolean transitionTo(NotificationStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            return false;
        }
        
        // Record timestamp based on the new state
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case SENT:
                this.sentAt = now;
                break;
            case DELIVERED:
                this.deliveredAt = now;
                break;
            case READ:
                this.readAt = now;
                break;
            default:
                // No timestamp for other states
                break;
        }
        
        this.status = newStatus;
        return true;
    }
    
    /**
     * Marks the notification as read.
     * 
     * @return true if the transition to READ state was successful, false otherwise
     */
    public boolean markAsRead() {
        return transitionTo(NotificationStatus.READ);
    }
    
    /**
     * Marks the notification as delivered.
     * 
     * @return true if the transition to DELIVERED state was successful, false otherwise
     */
    public boolean markAsDelivered() {
        return transitionTo(NotificationStatus.DELIVERED);
    }
    
    /**
     * Archives the notification.
     * 
     * @return true if the transition to ARCHIVED state was successful, false otherwise
     */
    public boolean archive() {
        return transitionTo(NotificationStatus.ARCHIVED);
    }

    /**
     * Sends the notification.
     * In a real implementation, this would involve some notification service.
     *
     * @return true if the notification was sent successfully
     */
    public boolean send() {
        // In a real application, this would send the notification via a notification service
        return transitionTo(NotificationStatus.SENT);
    }

    public LocalDateTime getTimestamp() {
        // Return the most recent timestamp based on status
        switch (status) {
            case READ:
                return readAt;
            case DELIVERED:
                return deliveredAt;
            case SENT:
                return sentAt;
            default:
                return createdAt;
        }
    }

    public boolean isRead() {
        return status.isRead();
    }
    
    public boolean isDelivered() {
        return status.isDelivered();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}