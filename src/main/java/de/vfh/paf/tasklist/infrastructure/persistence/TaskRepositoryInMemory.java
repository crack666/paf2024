package de.vfh.paf.tasklist.infrastructure.persistence;

import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the TaskRepository interface.
 * This is a simple implementation for testing and development purposes.
 */
public class TaskRepositoryInMemory implements TaskRepository {
    private final Map<Integer, Task> tasks = new HashMap<>();

    @Override
    public Task save(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(int id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAllByUserId(int userId) {
        return tasks.values().stream()
                .filter(task -> task.getAssignedUserId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findTasksByDependency(int taskId) {
        return tasks.values().stream()
                .filter(task -> task.getDependencies().stream()
                        .anyMatch(dependency -> dependency.getId() == taskId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findOverdueTasks(LocalDateTime currentTime) {
        return tasks.values().stream()
                .filter(task -> !task.isCompleted() && task.getDueDate().isBefore(currentTime))
                .collect(Collectors.toList());
    }
}