package de.vfh.paf.tasklist.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldCreateTaskWithRequiredFields() {
        // Arrange
        int id = 1;
        String title = "Test Task";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        int assignedUserId = 100;
        Status status = Status.CREATED;

        // Act
        Task task = new Task(id, title, dueDate, assignedUserId);

        // Assert
        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(assignedUserId, task.getAssignedUserId());
        assertEquals(status, task.getStatus());
        assertFalse(task.isCompleted());
        assertNotNull(task.getCreatedAt());
        assertNull(task.getDescription());
        assertNotNull(task.getDependencies());
        assertTrue(task.getDependencies().isEmpty());
    }

    @Test
    void shouldCreateTaskWithAllFields() {
        // Arrange
        int id = 1;
        String title = "Test Task";
        String description = "This is a test task";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        boolean isCompleted = false;
        Status status = Status.CREATED;
        int assignedUserId = 100;
        String taskClassName = "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask";
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);

        // Act
        Task task = new Task(id, title, description, dueDate, isCompleted, 
                            status, assignedUserId, taskClassName, scheduledTime);

        // Assert
        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(isCompleted, task.isCompleted());
        assertEquals(status, task.getStatus());
        assertEquals(assignedUserId, task.getAssignedUserId());
        assertEquals(taskClassName, task.getTaskClassName());
        assertEquals(scheduledTime, task.getScheduledTime());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getDependencies());
        assertTrue(task.getDependencies().isEmpty());
    }

    @Test
    void shouldMarkTaskAsComplete() {
        // Arrange
        Task task = new Task(1, "Test Task", LocalDateTime.now().plusDays(1), 100);
        assertFalse(task.isCompleted());

        // Act
        task.markComplete();

        // Assert
        assertTrue(task.isCompleted());
        assertEquals(Status.DONE, task.getStatus());
    }

    @Test
    void shouldUpdateTaskDetails() {
        // Arrange
        Task task = new Task(1, "Test Task", LocalDateTime.now().plusDays(1), 100);
        String newTitle = "Updated Task";
        String newDescription = "Updated description";
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(2);
        Status newStatus = Status.QUEUED;

        // Act
        task.updateDetails(newTitle, newDescription, newDueDate, newStatus);

        // Assert
        assertEquals(newTitle, task.getTitle());
        assertEquals(newDescription, task.getDescription());
        assertEquals(newDueDate, task.getDueDate());
        assertEquals(newStatus, task.getStatus());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    void shouldAddAndRemoveDependency() {
        // Arrange
        Task task = new Task(1, "Test Task", LocalDateTime.now().plusDays(1), 100);
        Task dependency = new Task(2, "Dependency Task", LocalDateTime.now().plusDays(1), 100);

        // Act - Add dependency
        task.addDependency(dependency);

        // Assert
        assertEquals(1, task.getDependencies().size());
        assertTrue(task.getDependencies().contains(dependency));

        // Act - Remove dependency
        task.removeDependency(dependency);

        // Assert
        assertEquals(0, task.getDependencies().size());
        assertFalse(task.getDependencies().contains(dependency));
    }
}