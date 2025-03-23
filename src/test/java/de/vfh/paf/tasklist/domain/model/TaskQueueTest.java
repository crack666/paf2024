package de.vfh.paf.tasklist.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskQueueTest {

    @Test
    void shouldCreateTaskQueue() {
        // Arrange
        int id = 1;
        String name = "Priority Queue";

        // Act
        TaskQueue taskQueue = new TaskQueue(id, name);

        // Assert
        assertEquals(id, taskQueue.getId());
        assertEquals(name, taskQueue.getName());
        assertNotNull(taskQueue.getTasks());
        assertTrue(taskQueue.getTasks().isEmpty());
        assertNotNull(taskQueue.getCreatedAt());
    }

    @Test
    void shouldEnqueueAndDequeueTask() {
        // Arrange
        TaskQueue taskQueue = new TaskQueue(1, "FIFO Queue");
        Task task1 = new Task(1, "Task 1", "Description 1", LocalDateTime.now().plusDays(1), null,  "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask",  LocalDateTime.now());
        Task task2 = new Task(2, "Task 2", "Description 2", LocalDateTime.now().plusDays(1), null,  "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask",  LocalDateTime.now());

        // Act - Enqueue tasks
        taskQueue.enqueueTask(task1);
        taskQueue.enqueueTask(task2);

        // Assert
        assertEquals(2, taskQueue.getTasks().size());
        assertEquals(task1, taskQueue.peekNextTask());

        // Act - Dequeue task
        Task dequeuedTask = taskQueue.dequeueTask();

        // Assert
        assertEquals(task1, dequeuedTask);
        assertEquals(1, taskQueue.getTasks().size());
        assertEquals(task2, taskQueue.peekNextTask());
        assertEquals(Status.RUNNING, dequeuedTask.getStatus());

        // Act - Dequeue another task
        dequeuedTask = taskQueue.dequeueTask();

        // Assert
        assertEquals(task2, dequeuedTask);
        assertEquals(0, taskQueue.getTasks().size());
        assertNull(taskQueue.peekNextTask());
        assertEquals(Status.RUNNING, dequeuedTask.getStatus());
    }

    @Test
    void shouldRemoveTaskById() {
        // Arrange
        TaskQueue taskQueue = new TaskQueue(1, "Test Queue");
        Task task1 = new Task(1, "Task 1", "Description 1", LocalDateTime.now().plusDays(1), null,  "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask",  LocalDateTime.now());
        Task task2 = new Task(2, "Task 2", "Description 2", LocalDateTime.now().plusDays(1), null,  "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask",  LocalDateTime.now());

        taskQueue.enqueueTask(task1);
        taskQueue.enqueueTask(task2);

        // Act
        boolean removed = taskQueue.removeTask(1);

        // Assert
        assertTrue(removed);
        assertEquals(1, taskQueue.getTasks().size());
        assertEquals(task2, taskQueue.peekNextTask());

        // Act - Try to remove non-existent task
        removed = taskQueue.removeTask(3);

        // Assert
        assertFalse(removed);
        assertEquals(1, taskQueue.getTasks().size());
    }

    @Test
    void shouldReorderTasksByDueDate() {
        // Arrange
        TaskQueue taskQueue = new TaskQueue(1, "Priority Queue");
        Task task1 = new Task(1, "Task 1 -- Third", "Description 1", LocalDateTime.now().plusDays(3), null,  "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask",
                LocalDateTime.now());
        Task task2 = new Task(2, "Task 2 -- First", "Description 2", LocalDateTime.now().plusDays(1), null,  "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask",
                LocalDateTime.now());
        Task task3 = new Task(3, "Task 3 -- Second", "Description 3", LocalDateTime.now().plusDays(2), null,  "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask",
                LocalDateTime.now());

        taskQueue.enqueueTask(task1);
        taskQueue.enqueueTask(task2);
        taskQueue.enqueueTask(task3);

        // Initial order check
        List<Task> tasks = taskQueue.getTasks();
        assertEquals(task1, tasks.get(0));
        assertEquals(task2, tasks.get(1));
        assertEquals(task3, tasks.get(2));

        // Act
        taskQueue.reorderTasks("dueDate");

        // Assert
        tasks = taskQueue.getTasks();
        assertEquals(task2, tasks.get(0)); // Earliest due date
        assertEquals(task3, tasks.get(1)); // Middle due date
        assertEquals(task1, tasks.get(2)); // Latest due date
        assertNotNull(taskQueue.getUpdatedAt());
    }
}