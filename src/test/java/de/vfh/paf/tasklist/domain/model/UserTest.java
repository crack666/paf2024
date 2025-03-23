package de.vfh.paf.tasklist.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUser() {
        // Arrange
        int id = 1;
        String name = "John Doe";
        String email = "john.doe@example.com";

        // Act
        User user = new User(id, name, email);

        // Assert
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertNotNull(user.getTasks());
        assertTrue(user.getTasks().isEmpty());
        assertNotNull(user.getNotifications());
        assertTrue(user.getNotifications().isEmpty());
    }

    @Test
    void shouldUpdateContactInfo() {
        // Arrange
        User user = new User(1, "John Doe", "john.doe@example.com");
        String newName = "Johnny Doe";
        String newEmail = "johnny.doe@example.com";

        // Act
        user.updateContactInfo(newName, newEmail);

        // Assert
        assertEquals(newName, user.getName());
        assertEquals(newEmail, user.getEmail());
    }

    @Test
    void shouldAddAndGetTask() {
        // Arrange
        User user = new User(1, "John Doe", "john.doe@example.com");
        Task task = new Task(1, "Test Task", null, 1);

        // Act
        user.addTask(task);
        List<Task> tasks = user.getTasks();

        // Assert
        assertEquals(1, tasks.size());
        assertTrue(tasks.contains(task));
    }

    @Test
    void shouldAddAndGetNotification() {
        // Arrange
        User user = new User(1, "John Doe", "john.doe@example.com");
        Notification notification = new Notification(1, 1, "Test notification");

        // Act
        user.addNotification(notification);
        List<Notification> notifications = user.getNotifications();

        // Assert
        assertEquals(1, notifications.size());
        assertTrue(notifications.contains(notification));
    }
}