package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Task entities.
 */
public interface TaskRepository {
    /**
     * Saves a task.
     *
     * @param task The task to save
     * @return The saved task
     */
    Task save(Task task);

    /**
     * Finds a task by its ID.
     *
     * @param id The ID of the task to find
     * @return An Optional containing the task if found, or empty if not found
     */
    Optional<Task> findById(int id);

    /**
     * Finds all tasks assigned to a specific user.
     *
     * @param userId The ID of the user
     * @return A list of tasks assigned to the user
     */
    List<Task> findAllByUserId(int userId);

    /**
     * Finds all tasks that depend on a specific task.
     *
     * @param taskId The ID of the dependency task
     * @return A list of tasks that depend on the specified task
     */
    List<Task> findTasksByDependency(int taskId);

    /**
     * Finds all tasks that are overdue.
     *
     * @param currentTime The current time to compare with task due dates
     * @return A list of overdue tasks
     */
    List<Task> findOverdueTasks(LocalDateTime currentTime);
}