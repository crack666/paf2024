package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.RunnableTask;
import de.vfh.paf.tasklist.domain.tasks.CalculatePiTask;
import de.vfh.paf.tasklist.domain.tasks.GenerateReportTask;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory for all available task types.
 * This service maintains a list of all task types that can be executed by the system.
 */
@Service
public class TaskFactory {

    private final Map<String, RunnableTask> taskTypes = new HashMap<>();

    /**
     * Initializes the factory with available task types.
     */
    public TaskFactory() {
        // Register available task types
        registerTaskType(new CalculatePiTask());
        registerTaskType(new GenerateReportTask());
    }

    /**
     * Registers a new task type.
     *
     * @param taskType The task implementation to register
     */
    public void registerTaskType(RunnableTask taskType) {
        taskTypes.put(taskType.getClass().getName(), taskType);
    }

    /**
     * Gets a task implementation by its class name.
     *
     * @param className The fully qualified class name of the task
     * @return The task implementation or null if not found
     */
    public RunnableTask getTaskType(String className) {
        return taskTypes.get(className);
    }

    /**
     * Gets all available task types as a map of class name to task name.
     *
     * @return Map of class name to friendly task name
     */
    public Map<String, String> getTaskTypeMap() {
        return taskTypes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getName()
                ));
    }
}