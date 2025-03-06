package de.vfh.paf.tasklist.domain.model;

/**
 * Interface for executable tasks.
 * Classes implementing this interface can be executed by the task system.
 */
public interface RunnableTask {
    
    /**
     * Executes the task and returns a task result.
     *
     * @param task The task object containing metadata
     * @return The result of the task execution
     */
    TaskResult run(Task task);
    
    /**
     * Returns the friendly name of this task type.
     *
     * @return A human-readable name of the task
     */
    String getName();
    
    /**
     * Returns a description of what this task does.
     *
     * @return A human-readable description
     */
    String getDescription();
}