package de.vfh.paf.tasklist.presentation.websocket;

import de.vfh.paf.tasklist.application.dto.TaskDTO;
import de.vfh.paf.tasklist.application.dto.TaskResultDTO;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.tasks.CalculatePiTask;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket controller for task-related real-time updates.
 */
@Controller
public class TaskWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public TaskWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a task status update to clients.
     *
     * @param task The task that has been updated
     */
    public void sendTaskStatusUpdate(Task task) {
        try {
            // Convert to DTO for the client
            TaskDTO taskDTO = new TaskDTO(task);

            // Send to the general task update topic
            messagingTemplate.convertAndSend("/topic/tasks/status", taskDTO);

            // Send to the specific task topic
            messagingTemplate.convertAndSend("/topic/tasks/" + task.getId() + "/status", taskDTO);

            // If the task belongs to a user, send to their personalized topic
            if (task.getAssignedUserId() != null) {
                messagingTemplate.convertAndSend("/user/" + task.getAssignedUserId() + "/tasks", taskDTO);
            }
        } catch (Exception e) {
            // Log the error but don't let it crash the application
            System.err.println("Error sending task status update for task ID " + task.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Sends a task result update to clients.
     *
     * @param task   The task
     * @param result The result of the task execution
     */
    public void sendTaskResultUpdate(Task task, TaskResult result) {
        if (task == null || result == null) {
            return;
        }

        try {
            TaskResultDTO resultDTO = new TaskResultDTO(result);
            Map<String, Object> payload = Map.of(
                    "taskId", task.getId(),
                    "result", resultDTO,
                    "status", task.getStatus().toString()
            );

            // Send to the general task results topic
            messagingTemplate.convertAndSend("/topic/tasks/results", payload);

            // Send to the specific task topic
            messagingTemplate.convertAndSend("/topic/tasks/" + task.getId() + "/result", payload);

            // If the task belongs to a user, send to their personalized topic
            if (task.getAssignedUserId() != null) {
                messagingTemplate.convertAndSend("/user/" + task.getAssignedUserId() + "/task-results", payload);
            }
        } catch (Exception e) {
            // Log the error but don't let it crash the application
            System.err.println("Error sending task result update for task ID " + task.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Sends a queue update to clients.
     *
     * @param queueId The ID of the queue that has been updated
     * @param task    The task that caused the update (can be null for general updates)
     * @param action  The action that occurred (e.g., "ADDED", "REMOVED", "STARTED", "COMPLETED")
     */
    public void sendQueueUpdate(int queueId, Task task, String action) {
        try {
            Map<String, Object> payload;

            if (task != null) {
                payload = Map.of(
                        "queueId", queueId,
                        "action", action,
                        "taskId", task.getId(),
                        "taskStatus", task.getStatus().toString(),
                        "timestamp", System.currentTimeMillis()
                );
            } else {
                payload = Map.of(
                        "queueId", queueId,
                        "action", action,
                        "timestamp", System.currentTimeMillis()
                );
            }

            // Send to the general queues topic
            messagingTemplate.convertAndSend("/topic/queues", payload);

            // Send to the specific queue topic
            messagingTemplate.convertAndSend("/topic/queues/" + queueId, payload);
        } catch (Exception e) {
            // Log the error but don't let it crash the application
            System.err.println("Error sending queue update for queue ID " + queueId + ": " + e.getMessage());
        }
    }

    /**
     * Sends a task progress update to clients.
     *
     * @param taskId         The ID of the task
     * @param progressDataObj   The progress data to send
     */
    public void sendTaskProgressUpdate(int taskId, Object progressDataObj) {
        try {
            // Konvertiere das progressDataObj (z.B. vom Typ CalculatePiTask.ProgressData)
            // in eine Map, die zusätzlich die taskId enthält.
            CalculatePiTask.ProgressData progressData = (CalculatePiTask.ProgressData) progressDataObj;
            Map<String, Object> payload = new HashMap<>();
            payload.put("taskId", taskId);
            payload.put("totalIterations", progressData.getTotalIterations());
            payload.put("currentIteration", progressData.getCurrentIteration());
            payload.put("startTime", progressData.getStartTime());
            payload.put("currentValue", progressData.getCurrentValue());
            payload.put("finalValue", progressData.getFinalValue());
            payload.put("progressPercentage", progressData.getProgressPercentage());
            payload.put("elapsedTimeMillis", progressData.getElapsedTimeMillis());
            payload.put("estimatedTimeRemainingMillis", progressData.getEstimatedTimeRemainingMillis());

            // Falls progressData selbst dieses Feld nicht enthält, kann es aus dem Event extrahiert werden.
            // Nehmen wir an, dass der TaskProgressEvent den completed-Status bereits enthält,
            // und dieser in der Nachricht enthalten ist. Falls nicht, kann man hier z.B. prüfen,
            // ob currentIteration == totalIterations:
            boolean completed = (progressData.getCurrentIteration() >= progressData.getTotalIterations());
            payload.put("completed", completed);

            // Sende alle Fortschrittsupdates an einen festen Topic
            messagingTemplate.convertAndSend("/topic/tasks/progress", payload);
        } catch (Exception e) {
            System.err.println("Error sending task progress update for task ID " + taskId + ": " + e.getMessage());
        }
    }
}