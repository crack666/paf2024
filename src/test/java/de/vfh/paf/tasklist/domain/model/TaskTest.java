package de.vfh.paf.tasklist.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldCreateTaskWithRequiredFields() {
        // Arrange
        int id = 1;
        String title = "Test Task";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        int assignedUserId = 100;
        TaskStatus taskStatus = TaskStatus.CREATED;

        // Act
        Task task = new Task(1, title, "Description 1", dueDate, assignedUserId, "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask");

        // Assert
        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(assignedUserId, task.getAssignedUserId());
        assertEquals(taskStatus, task.getStatus());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getDescription());
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
        TaskStatus taskStatus = TaskStatus.CREATED;
        int assignedUserId = 100;
        String taskClassName = "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask";

        // Act
        Task task = new Task(id, title, description, dueDate, taskStatus, assignedUserId, taskClassName);

        // Assert
        assertEquals(id, task.getId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(dueDate, task.getDueDate());
        assertEquals(taskStatus, task.getStatus());
        assertEquals(assignedUserId, task.getAssignedUserId());
        assertEquals(taskClassName, task.getTaskClassName());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getDependencies());
        assertTrue(task.getDependencies().isEmpty());
    }

    @Test
    void shouldMarkTaskAsComplete() {
        // Arrange
        Task task = new Task(1, "Test Task", "Description 1", LocalDateTime.now().plusDays(1), null, "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask");
        assertEquals(TaskStatus.CREATED, task.getStatus());

        // Act
        task.markComplete();

        // Assert
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void shouldUpdateTaskDetails() {
        // Arrange
        Task task = new Task(1, "Test Task", "Description 1", LocalDateTime.now().plusDays(1), null, "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask");
        String newTitle = "Updated Task";
        String newDescription = "Updated description";
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(2);
        TaskStatus newTaskStatus = TaskStatus.QUEUED;

        // Act
        task.updateDetails(newTitle, newDescription, newDueDate, newTaskStatus);

        // Assert
        assertEquals(newTitle, task.getTitle());
        assertEquals(newDescription, task.getDescription());
        assertEquals(newDueDate, task.getDueDate());
        assertEquals(newTaskStatus, task.getStatus());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    void shouldAddAndRemoveDependency() {
        // Arrange
        Task task = new Task(1, "Test Task", "Description 1", LocalDateTime.now().plusDays(1), null, "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask");
        Task dependency = new Task(2, "Dependency Task", "Description 1", LocalDateTime.now().plusDays(1), null, "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask");

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