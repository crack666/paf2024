package de.vfh.paf.tasklist.presentation.websocket;

import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Test mock of the WebSocket controller for task-related real-time updates.
 * This mock is used only for testing and does not actually send WebSocket messages.
 */
@Component
@Primary
@Profile("test")
public class TaskWebSocketController {

    /**
     * Default constructor for the test mock.
     */
    public TaskWebSocketController() {
        // No messaging template needed for test mock
    }

    /**
     * Mock method that doesn't send any actual WebSocket messages.
     *
     * @param task The task that has been updated
     */
    public void sendTaskStatusUpdate(Task task) {
        // Do nothing - this is a mock for testing
    }

    /**
     * Mock method that doesn't send any actual WebSocket messages.
     *
     * @param task The task
     * @param result The result of the task execution
     */
    public void sendTaskResultUpdate(Task task, TaskResult result) {
        // Do nothing - this is a mock for testing
    }

    /**
     * Mock method that doesn't send any actual WebSocket messages.
     * 
     * @param queueId The ID of the queue that has been updated
     * @param task The task that caused the update (can be null for general updates)
     * @param action The action that occurred (e.g., "ADDED", "REMOVED", "STARTED", "COMPLETED")
     */
    public void sendQueueUpdate(int queueId, Task task, String action) {
        // Do nothing - this is a mock for testing
    }
}