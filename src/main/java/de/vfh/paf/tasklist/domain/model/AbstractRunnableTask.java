package de.vfh.paf.tasklist.domain.model;

import java.time.LocalDateTime;
import java.util.logging.Logger;

public abstract class AbstractRunnableTask implements RunnableTask {

    private static final Logger logger = Logger.getLogger(AbstractRunnableTask.class.getName());

    @Override
    public final TaskResult run(Task task) {
        String taskInfo = String.format("Task started: [%s] %s", getName(), task.getTitle());
        logger.info(taskInfo);

        LocalDateTime start = LocalDateTime.now();
        try {
            TaskResult result = execute(task);

            logger.info(String.format("Task completed: [%s] %s", getName(), task.getTitle()));
            return result;
        } catch (Exception e) {
            logger.severe(String.format("Task failed: [%s] %s - %s", getName(), task.getTitle(), e.getMessage()));
            throw e;
        } finally {
            LocalDateTime end = LocalDateTime.now();
            logger.info(String.format("Execution time: %d ms",
                    java.time.Duration.between(start, end).toMillis()));
        }
    }

    /**
     * Implemented by subclasses with actual task logic.
     */
    protected abstract TaskResult execute(Task task);
}
