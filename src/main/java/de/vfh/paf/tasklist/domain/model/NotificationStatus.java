package de.vfh.paf.tasklist.domain.model;

/**
 * Represents the possible states of a notification in the system.
 * Implements the State Pattern to model the lifecycle of notifications.
 */
public enum NotificationStatus {
    /**
     * Notification has been created but not yet sent.
     */
    CREATED,
    
    /**
     * Notification has been sent to the messaging system but delivery is not confirmed.
     */
    SENT,
    
    /**
     * Notification has been delivered to the client but not yet read.
     */
    DELIVERED,
    
    /**
     * Notification has been read by the user.
     */
    READ,
    
    /**
     * Notification has been archived by the user.
     */
    ARCHIVED;
    
    /**
     * Checks if the notification can be transitioned to the given status.
     *
     * @param nextStatus The status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(NotificationStatus nextStatus) {
        return switch (this) {
            case CREATED ->
                // From CREATED, can only move to SENT
                    nextStatus == SENT;
            case SENT ->
                // From SENT, can move to DELIVERED or directly to READ (if client reads instantly)
                    nextStatus == DELIVERED || nextStatus == READ;
            case DELIVERED ->
                // From DELIVERED, can only move to READ
                    nextStatus == READ;
            case READ ->
                // From READ, can only move to ARCHIVED
                    nextStatus == ARCHIVED;
            case ARCHIVED ->
                // Cannot transition from ARCHIVED
                    false;
            default -> false;
        };
    }
    
    /**
     * Checks if the notification is in a terminal state.
     *
     * @return true if the notification cannot transition further
     */
    public boolean isTerminal() {
        return this == ARCHIVED;
    }
    
    /**
     * Checks if the notification has been read.
     *
     * @return true if the notification has been read
     */
    public boolean isRead() {
        return this == READ || this == ARCHIVED;
    }
    
    /**
     * Checks if the notification has been delivered or read.
     *
     * @return true if the notification has been delivered or is in a later state
     */
    public boolean isDelivered() {
        return this == DELIVERED || this == READ || this == ARCHIVED;
    }
}