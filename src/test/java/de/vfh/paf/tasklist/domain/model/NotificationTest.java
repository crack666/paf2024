package de.vfh.paf.tasklist.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

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
        assertNotNull(notification.getSentAt());
        assertFalse(notification.isRead());
    }
    
    @Test
    void shouldMarkAsRead() {
        // Arrange
        Notification notification = new Notification(1, 5, "Test notification");
        assertFalse(notification.isRead());
        
        // Act
        notification.markAsRead();
        
        // Assert
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
        assertNotNull(notification.getSentAt());
    }
}