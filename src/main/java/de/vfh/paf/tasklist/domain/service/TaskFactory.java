package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.RunnableTask;
import de.vfh.paf.tasklist.domain.tasks.CalculatePiTask;
import de.vfh.paf.tasklist.domain.tasks.GenerateReportTask;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Factory for all available task types.
 * This service maintains a registry of suppliers for task types
 * that can be executed by the system. Each call to getTaskType() returns a new instance.
 */
@Service
public class TaskFactory {

    private final Map<String, Supplier<? extends RunnableTask>> taskTypesRegistry = new HashMap<>();

    /**
     * Initializes the factory with available task types.
     */
    public TaskFactory() {
        // Register available task types using their suppliers
        registerTaskType(CalculatePiTask.class.getName(), CalculatePiTask::new);
        registerTaskType(GenerateReportTask.class.getName(), GenerateReportTask::new);
    }

    /**
     * Registers a new task type supplier.
     *
     * @param className The fully qualified class name of the task.
     * @param supplier  A supplier that produces new instances of the task.
     */
    public void registerTaskType(String className, Supplier<? extends RunnableTask> supplier) {
        taskTypesRegistry.put(className, supplier);
    }

    /**
     * Gets a new task implementation instance by its class name.
     *
     * @param className The fully qualified class name of the task.
     * @return A new instance of the task implementation, or null if not found.
     */
    public RunnableTask getTaskType(String className) {
        Supplier<? extends RunnableTask> supplier = taskTypesRegistry.get(className);
        return supplier != null ? supplier.get() : null;
    }

    /**
     * Gets all available task types as a map of class name to friendly task name.
     *
     * @return Map of class name to friendly task name.
     */
    public Map<String, String> getTaskTypeMap() {
        return taskTypesRegistry.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get().getName()
                ));
    }
}