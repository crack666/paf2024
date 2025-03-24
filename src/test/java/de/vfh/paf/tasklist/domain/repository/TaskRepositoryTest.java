package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.TaskStatus;
import de.vfh.paf.tasklist.domain.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();

        task1 = new Task();
        task1.setTitle("Task 1");
        task1.setDueDate(LocalDateTime.now().plusDays(1));
        task1.setAssignedUserId(100);
        task1.setCreatedAt(LocalDateTime.now());

        task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDueDate(LocalDateTime.now().plusDays(2));
        task2.setAssignedUserId(100);
        task2.setCreatedAt(LocalDateTime.now());

        task1 = taskRepository.save(task1);
        task2 = taskRepository.save(task2);
    }

    @Test
    void shouldSaveTask() {
        // Arrange
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDueDate(LocalDateTime.now().plusDays(3));
        newTask.setAssignedUserId(200);
        newTask.setCreatedAt(LocalDateTime.now());

        // Act
        Task savedTask = taskRepository.save(newTask);

        // Assert
        assertNotNull(savedTask.getId());
        assertEquals("New Task", savedTask.getTitle());
    }

    @Test
    void shouldFindTaskById() {
        // Act
        Optional<Task> foundTask = taskRepository.findById(task1.getId());

        // Assert
        assertTrue(foundTask.isPresent());
        assertEquals(task1.getId(), foundTask.get().getId());
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentTask() {
        // Act
        Optional<Task> foundTask = taskRepository.findById(9999);

        // Assert
        assertFalse(foundTask.isPresent());
    }

    @Test
    void shouldFindAllTasksByUserId() {
        // Arrange
        Task task3 = new Task();
        task3.setTitle("Task 3");
        task3.setDueDate(LocalDateTime.now().plusDays(3));
        task3.setAssignedUserId(200);
        task3.setCreatedAt(LocalDateTime.now());
        taskRepository.save(task3);

        // Act
        List<Task> tasks = taskRepository.findAllByAssignedUserId(100);

        // Assert
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().anyMatch(t -> t.getId().equals(task1.getId())));
        assertTrue(tasks.stream().anyMatch(t -> t.getId().equals(task2.getId())));
        assertFalse(tasks.stream().anyMatch(t -> t.getId().equals(task3.getId())));
    }

    @Test
    void shouldFindTasksByDependency() {
        // Arrange
        Task dependency = new Task();
        dependency.setTitle("Dependency Task");
        dependency.setDueDate(LocalDateTime.now().plusDays(1));
        dependency.setAssignedUserId(100);
        dependency.setCreatedAt(LocalDateTime.now());
        dependency = taskRepository.save(dependency);

        task1.addDependency(dependency);
        task2.addDependency(dependency);
        taskRepository.save(task1);
        taskRepository.save(task2);

        // Act
        List<Task> dependentTasks = taskRepository.findTasksByDependency(dependency.getId());

        // Assert
        assertEquals(2, dependentTasks.size());
        assertTrue(dependentTasks.stream().anyMatch(t -> t.getId().equals(task1.getId())));
        assertTrue(dependentTasks.stream().anyMatch(t -> t.getId().equals(task2.getId())));
    }

    @Test
    void shouldFindOverdueTasks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        Task overdueTask1 = new Task();
        overdueTask1.setTitle("Overdue Task 1");
        overdueTask1.setDueDate(now.minusDays(1));
        overdueTask1.setAssignedUserId(100);
        overdueTask1.setCreatedAt(LocalDateTime.now());

        Task overdueTask2 = new Task();
        overdueTask2.setTitle("Overdue Task 2");
        overdueTask2.setDueDate(now.minusHours(2));
        overdueTask2.setAssignedUserId(100);
        overdueTask2.setCreatedAt(LocalDateTime.now());

        taskRepository.save(overdueTask1);
        taskRepository.save(overdueTask2);

        // Act
        List<Task> overdueTasks = taskRepository.findOverdueTasks(now);

        // Assert
        assertEquals(2, overdueTasks.size());
        assertTrue(overdueTasks.stream().anyMatch(t -> t.getTitle().equals("Overdue Task 1")));
        assertTrue(overdueTasks.stream().anyMatch(t -> t.getTitle().equals("Overdue Task 2")));
        assertFalse(overdueTasks.stream().anyMatch(t -> t.getTitle().equals("Task 1")));
    }
}