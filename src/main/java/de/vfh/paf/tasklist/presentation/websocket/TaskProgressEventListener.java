package de.vfh.paf.tasklist.presentation.websocket;

import de.vfh.paf.tasklist.domain.events.TaskProgressEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TaskProgressEventListener {

    private final TaskWebSocketController taskWebSocketController;

    public TaskProgressEventListener(TaskWebSocketController taskWebSocketController) {
        this.taskWebSocketController = taskWebSocketController;
    }

    @EventListener
    public void handleTaskProgressEvent(TaskProgressEvent event) {
        // Hier könnte man ein DTO bauen, falls nötig – für dieses Beispiel senden wir die ProgressData direkt
        taskWebSocketController.sendTaskProgressUpdate(event.getTaskId(), event.getProgressData());
    }
}
