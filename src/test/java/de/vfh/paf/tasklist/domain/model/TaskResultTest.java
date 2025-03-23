package de.vfh.paf.tasklist.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskResultTest {

    @Test
    void shouldCreateTaskResult() {
        // Arrange
        int id = 1;
        String name = "Pi Calculation";
        String resultValue = "3.14159265359";
        int taskId = 5;

        // Act
        TaskResult taskResult = new TaskResult(id, name, resultValue, taskId);

        // Assert
        assertEquals(id, taskResult.getId());
        assertEquals(name, taskResult.getName());
        assertEquals(resultValue, taskResult.getResultValue());
        assertEquals(taskId, taskResult.getTaskId());
        assertNotNull(taskResult.getComputedAt());
    }

    @Test
    void shouldGetResult() {
        // Arrange
        TaskResult taskResult = new TaskResult(1, "Current Time", "14:30:00", 5);

        // Act
        String result = taskResult.getResult();

        // Assert
        assertEquals(taskResult.getResultValue(), result);
    }
}