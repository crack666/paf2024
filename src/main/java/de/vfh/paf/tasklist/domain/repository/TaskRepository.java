package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Task entities using JPA.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    /**
     * Override findById to eagerly fetch dependencies
     * 
     * @param id The ID of the task
     * @return Optional containing the task if found
     */
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.dependencies WHERE t.id = :id")
    Optional<Task> findById(@Param("id") Integer id);
    
    /**
     * Override findAll to eagerly fetch dependencies
     * 
     * @return List of all tasks with their dependencies
     */
    @Override
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.dependencies")
    List<Task> findAll();

    /**
     * Finds all tasks assigned to a specific user.
     *
     * @param userId The ID of the user
     * @return A list of tasks assigned to the user
     */
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.dependencies WHERE t.assignedUserId = :userId")
    List<Task> findAllByAssignedUserId(@Param("userId") Integer userId);

    /**
     * Finds all tasks that depend on a specific task.
     *
     * @param taskId The ID of the dependency task
     * @return A list of tasks that depend on the specified task
     */
    @Query("SELECT DISTINCT t FROM Task t JOIN t.dependencies d LEFT JOIN FETCH t.dependencies WHERE d.id = :taskId")
    List<Task> findTasksByDependency(@Param("taskId") Integer taskId);

    /**
     * Finds all tasks that are overdue.
     *
     * @param currentTime The current time to compare with task due dates
     * @return A list of overdue tasks
     */
    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.dependencies WHERE t.taskStatus <> 'DONE' AND t.dueDate < :currentTime")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);
}