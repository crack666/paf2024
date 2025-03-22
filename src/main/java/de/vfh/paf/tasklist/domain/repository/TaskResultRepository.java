package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.TaskResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing task results.
 */
@Repository
public interface TaskResultRepository extends JpaRepository<TaskResult, Integer> {
    
    /**
     * Find all results for a specific task.
     *
     * @param taskId The ID of the task
     * @return List of task results
     */
    List<TaskResult> findByTaskId(Integer taskId);
}