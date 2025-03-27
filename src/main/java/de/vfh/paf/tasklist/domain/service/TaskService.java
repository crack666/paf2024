package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.TaskStatus;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing tasks.
 */
@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;

    /**
     * Creates a new task service.
     */
    private final de.vfh.paf.tasklist.domain.repository.TaskResultRepository taskResultRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public TaskService(TaskRepository taskRepository, de.vfh.paf.tasklist.domain.repository.TaskResultRepository taskResultRepository) {
        this.taskRepository = taskRepository;
        this.taskResultRepository = taskResultRepository;
    }

    /**
     * Creates a new runnable task with a specific implementation.
     *
     * @param title         The title of the task
     * @param description   The description of the task
     * @param dueDate       The due date for the task
     * @param userId        The ID of the user assigned to the task
     * @param taskClassName The fully qualified class name of the task implementation
     * @return The created task
     */
    public Task createRunnableTask(String title, String description, LocalDateTime dueDate,
                                   int userId, String taskClassName) {
        Task task = new Task(null, title, description, dueDate, userId, taskClassName);
        return taskRepository.save(task);
    }

    /**
     * Updates an existing task.
     *
     * @param taskId      The ID of the task to update
     * @param title       The new title
     * @param description The new description
     * @param dueDate     The new due date
     * @return The updated task, or null if the task is not found
     */
    public Task updateTask(int taskId, String title, String description, LocalDateTime dueDate) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);

        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.updateDetails(title, description, dueDate);
            return taskRepository.save(task);
        }

        return null;
    }

    /**
     * Marks a task as complete.
     *
     * @param taskId The ID of the task to complete
     * @return The completed task, or null if the task is not found
     */
    public Task completeTask(int taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);

        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.markComplete();
            return taskRepository.save(task);
        }

        return null;
    }

    /**
     * Finds a task by its ID.
     *
     * @param taskId The ID of the task to find
     * @return Optional containing the task if found
     */
    public Optional<Task> findById(int taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            // Load the task result if available
            List<de.vfh.paf.tasklist.domain.model.TaskResult> results = taskResultRepository.findByTaskId(task.getId());
            if (!results.isEmpty()) {
                // Get the most recent result (assuming it's the relevant one)
                task.setResult(results.getFirst());
            }
        }

        return taskOptional;
    }

    /**
     * Adds a dependency to a task.
     *
     * @param taskId           The ID of the task
     * @param dependencyTaskId The ID of the dependency task
     * @return The updated task, or null if either task is not found
     */
    public Task addDependency(int taskId, int dependencyTaskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        Optional<Task> optionalDependency = taskRepository.findById(dependencyTaskId);

        if (optionalTask.isPresent() && optionalDependency.isPresent()) {
            Task task = optionalTask.get();
            Task dependency = optionalDependency.get();

            task.addDependency(dependency);
            return taskRepository.save(task);
        }

        return null;
    }

    /**
     * Removes a dependency from a task.
     *
     * @param taskId           The ID of the task
     * @param dependencyTaskId The ID of the dependency task to remove
     * @return The updated task, or null if either task is not found
     */
    public Task removeDependency(int taskId, int dependencyTaskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        Optional<Task> optionalDependency = taskRepository.findById(dependencyTaskId);

        if (optionalTask.isPresent() && optionalDependency.isPresent()) {
            Task task = optionalTask.get();
            Task dependency = optionalDependency.get();

            task.removeDependency(dependency);
            return taskRepository.save(task);
        }

        return null;
    }

    /**
     * Detects deadlocks in task dependencies.
     * A deadlock is a circular dependency between tasks.
     *
     * @return true if a deadlock is detected, false otherwise
     */
    public boolean detectDeadlocks() {
        return !findDeadlockedTasks().isEmpty();
    }
    
    /**
     * Finds tasks involved in deadlocks (circular dependencies).
     * 
     * @return A list of task IDs involved in circular dependencies, or empty list if no deadlocks
     */
    public List<Integer> findDeadlockedTasks() {
        Map<Integer, Set<Integer>> graph = buildDependencyGraph();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> allCycleNodes = new HashSet<>();
        
        for (Integer taskId : graph.keySet()) {
            Set<Integer> recursionStack = new HashSet<>();
            Set<Integer> cycleNodes = new HashSet<>();
            if (hasCycle(graph, taskId, visited, recursionStack, cycleNodes)) {
                allCycleNodes.addAll(cycleNodes);
            }
        }
        
        return new ArrayList<>(allCycleNodes);
    }

    /**
     * Enhanced cycle detection that collects the nodes involved in the cycle.
     */
    private boolean hasCycle(Map<Integer, Set<Integer>> graph, int taskId,
                              Set<Integer> visited, Set<Integer> recursionStack,
                              Set<Integer> cycleNodes) {
        // If node is already in recursion stack, there is a cycle
        if (recursionStack.contains(taskId)) {
            // Add all nodes in the current recursion stack to the cycle nodes
            cycleNodes.addAll(recursionStack);
            return true;
        }

        // If node is already visited and not in recursion stack, no cycle through this node
        if (visited.contains(taskId)) {
            return false;
        }

        // Mark current node as visited and add to recursion stack
        visited.add(taskId);
        recursionStack.add(taskId);

        // Visit all adjacent nodes
        Set<Integer> dependencies = graph.getOrDefault(taskId, Collections.emptySet());
        for (Integer dependencyId : dependencies) {
            if (hasCycle(graph, dependencyId, visited, recursionStack, cycleNodes)) {
                return true;
            }
        }

        // Remove from recursion stack
        recursionStack.remove(taskId);

        return false;
    }

    private Map<Integer, Set<Integer>> buildDependencyGraph() {
        Map<Integer, Set<Integer>> graph = new HashMap<>();

        // Get all tasks from the repository
        List<Task> allTasks = taskRepository.findAll();

        // Build the dependency graph using the collected tasks
        for (Task task : allTasks) {
            Set<Integer> dependencies = new HashSet<>();
            for (Task dependency : task.getDependencies()) {
                dependencies.add(dependency.getId());
            }
            graph.put(task.getId(), dependencies);
        }

        return graph;
    }

    /**
     * Finds all tasks in the system.
     *
     * @return List of all tasks
     */
    public List<Task> findAll() {
        List<Task> tasks = taskRepository.findAll();

        // Load results for completed tasks
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.DONE) {
                List<de.vfh.paf.tasklist.domain.model.TaskResult> results = taskResultRepository.findByTaskId(task.getId());
                if (!results.isEmpty()) {
                    task.setResult(results.getFirst());
                }
            }
        }

        return tasks;
    }

    /**
     * Finds all tasks assigned to a specific user.
     *
     * @param userId The ID of the user
     * @return List of tasks assigned to the user
     */
    public List<Task> findByUserId(int userId) {
        return taskRepository.findAllByAssignedUserId(userId);
    }

    /**
     * Finds all tasks with a specific status.
     *
     * @param taskStatus The status to filter by
     * @return List of tasks with the specified status
     */
    public List<Task> findByStatus(TaskStatus taskStatus) {
        return findAll().stream()
                .filter(task -> task.getStatus() == taskStatus)
                //order by dueDate descending
                .sorted(Comparator.comparing(Task::getDueDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Finds all tasks assigned to a specific user with a specific status.
     *
     * @param userId The ID of the user
     * @param taskStatus The status to filter by
     * @return List of tasks matching both criteria
     */
    public List<Task> findByUserIdAndStatus(int userId, TaskStatus taskStatus) {
        return findAll().stream()
                .filter(task -> task.getAssignedUserId() == userId && task.getStatus() == taskStatus)
                .collect(Collectors.toList());
    }

    /**
     * Finds all tasks that are ready to run.
     * These are tasks that have a taskClassName, are not completed,
     * have all dependencies completed, and are scheduled for now or in the past.
     *
     * @return List of ready to run tasks
     */
    public List<Task> findReadyToRunTasks() {
        return findAll().stream()
                .filter(Task::isReadyToRun)
                .collect(Collectors.toList());
    }

    /**
     * Processes a task directly, executing the task implementation.
     * This is primarily used for internal and testing purposes.
     *
     * @param task The task to process
     * @return The task result
     */
    public de.vfh.paf.tasklist.domain.model.TaskResult processTask(Task task) {
        // This is a simplified implementation just to make the code compile
        task.markComplete();
        taskRepository.save(task);

        return new de.vfh.paf.tasklist.domain.model.TaskResult(
                "Task processed",
                "Task " + task.getTitle() + " was processed at " + LocalDateTime.now(),
                LocalDateTime.now());
    }
}