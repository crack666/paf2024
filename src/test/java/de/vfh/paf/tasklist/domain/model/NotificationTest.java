package de.vfh.paf.tasklist.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void shouldCreateNotification() {
        // Arrange
        int id = 1;
        int userId = 5;
        String message = "Your task is overdue";

        // Act
        Notification notification = new Notification(id, userId, message);

        // Assert
        assertEquals(id, notification.getId());
        assertEquals(userId, notification.getUserId());
        assertEquals(message, notification.getMessage());
        assertEquals(NotificationStatus.CREATED, notification.getStatus());
        assertFalse(notification.isRead());
    }

    @Test
    void shouldMarkAsRead() {
        // Arrange
        Notification notification = new Notification(1, 5, "Test notification");

        // First transition to sent state (can't go directly from CREATED to READ)
        notification.transitionTo(NotificationStatus.SENT);
        // Then to delivered state
        notification.transitionTo(NotificationStatus.DELIVERED);
        assertFalse(notification.isRead());

        // Act
        boolean marked = notification.markAsRead();

        // Assert
        assertTrue(marked);
        assertTrue(notification.isRead());
    }

    @Test
    void shouldSendNotification() {
        // Arrange
        Notification notification = new Notification(1, 5, "Test notification");

        // Act
        boolean result = notification.send();

        // Assert
        assertTrue(result);
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertNotNull(notification.getSentAt());
    }
}