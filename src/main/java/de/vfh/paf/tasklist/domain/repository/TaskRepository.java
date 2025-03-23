package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for managing Task entities using JPA.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    /**
     * Finds all tasks assigned to a specific user.
     *
     * @param userId The ID of the user
     * @return A list of tasks assigned to the user
     */
    List<Task> findAllByAssignedUserId(Integer userId);

    /**
     * Finds all tasks that depend on a specific task.
     *
     * @param taskId The ID of the dependency task
     * @return A list of tasks that depend on the specified task
     */
    @Query("SELECT t FROM Task t JOIN t.dependencies d WHERE d.id = :taskId")
    List<Task> findTasksByDependency(@Param("taskId") Integer taskId);

    /**
     * Finds all tasks that are overdue.
     *
     * @param currentTime The current time to compare with task due dates
     * @return A list of overdue tasks
     */
    @Query("SELECT t FROM Task t WHERE t.taskStatus <> 'DONE' AND t.dueDate < :currentTime")
    List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);
}