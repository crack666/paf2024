package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.infrastructure.persistence.TaskRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskRepositoryTest {
    private TaskRepository taskRepository;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        taskRepository = new TaskRepositoryInMemory();
        task1 = new Task(1, "Task 1", LocalDateTime.now().plusDays(1), 100);
        task2 = new Task(2, "Task 2", LocalDateTime.now().plusDays(2), 100);
    }

    @Test
    void shouldSaveTask() {
        // Act
        Task savedTask = taskRepository.save(task1);
        
        // Assert
        assertEquals(task1, savedTask);
    }

    @Test
    void shouldFindTaskById() {
        // Arrange
        taskRepository.save(task1);
        
        // Act
        Optional<Task> foundTask = taskRepository.findById(1);
        
        // Assert
        assertTrue(foundTask.isPresent());
        assertEquals(task1, foundTask.get());
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentTask() {
        // Act
        Optional<Task> foundTask = taskRepository.findById(99);
        
        // Assert
        assertFalse(foundTask.isPresent());
    }

    @Test
    void shouldFindAllTasksByUserId() {
        // Arrange
        taskRepository.save(task1);
        taskRepository.save(task2);
        Task task3 = new Task(3, "Task 3", LocalDateTime.now().plusDays(3), 200);
        taskRepository.save(task3);
        
        // Act
        List<Task> tasks = taskRepository.findAllByUserId(100);
        
        // Assert
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
        assertFalse(tasks.contains(task3));
    }

    @Test
    void shouldFindTasksByDependency() {
        // Arrange
        Task dependency = new Task(3, "Dependency Task", LocalDateTime.now().plusDays(1), 100);
        taskRepository.save(dependency);
        
        task1.addDependency(dependency);
        task2.addDependency(dependency);
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        // Act
        List<Task> dependentTasks = taskRepository.findTasksByDependency(3);
        
        // Assert
        assertEquals(2, dependentTasks.size());
        assertTrue(dependentTasks.contains(task1));
        assertTrue(dependentTasks.contains(task2));
    }

    @Test
    void shouldFindOverdueTasks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Task overdueTask1 = new Task(3, "Overdue Task 1", now.minusDays(1), 100);
        Task overdueTask2 = new Task(4, "Overdue Task 2", now.minusHours(2), 100);
        Task futureTask = new Task(5, "Future Task", now.plusDays(1), 100);
        
        taskRepository.save(overdueTask1);
        taskRepository.save(overdueTask2);
        taskRepository.save(futureTask);
        
        // Act
        List<Task> overdueTasks = taskRepository.findOverdueTasks(now);
        
        // Assert
        assertEquals(2, overdueTasks.size());
        assertTrue(overdueTasks.contains(overdueTask1));
        assertTrue(overdueTasks.contains(overdueTask2));
        assertFalse(overdueTasks.contains(futureTask));
    }
}