package de.vfh.paf.tasklist.domain.events;

import de.vfh.paf.tasklist.domain.tasks.CalculatePiTask;

public class TaskProgressEvent {
    private final int taskId;
    private final CalculatePiTask.ProgressData progressData;
    private final boolean completed;

    public TaskProgressEvent(int taskId, CalculatePiTask.ProgressData progressData, boolean completed) {
        this.taskId = taskId;
        this.progressData = progressData;
        this.completed = completed;
    }

    public int getTaskId() {
        return taskId;
    }

    public CalculatePiTask.ProgressData getProgressData() {
        return progressData;
    }

    public boolean isCompleted() {
        return completed;
    }
}