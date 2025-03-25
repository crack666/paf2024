package de.vfh.paf.tasklist.domain.tasks;

import de.vfh.paf.tasklist.domain.model.AbstractRunnableTask;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A task that calculates Pi to a specified number of decimal places.
 * This is a CPU-intensive task example with progress tracking.
 */
public class CalculatePiTask extends AbstractRunnableTask {

    private static final int DEFAULT_ITERATIONS = 1000;

    // Progress tracking for all running tasks (taskId -> progress data)
    private static final ConcurrentHashMap<Integer, ProgressData> taskProgress = new ConcurrentHashMap<>();

    /**
     * Gets the current progress of a task.
     *
     * @param taskId The ID of the task
     * @return The progress data, or null if task is not running
     */
    public static ProgressData getProgress(int taskId) {
        return taskProgress.get(taskId);
    }

    @Override
    protected TaskResult execute(Task task) {
        // Extract the number of iterations from task description, or use default
        int iterations = DEFAULT_ITERATIONS;
        try {
            if (task.getDescription() != null && task.getDescription().contains("iterations=")) {
                String[] parts = task.getDescription().split("iterations=");
                if (parts.length > 1) {
                    iterations = Integer.parseInt(parts[1].trim().split("\\s+")[0]);
                }
            }
        } catch (NumberFormatException e) {
            // Use default if parsing fails
        }

        // Setup progress tracking
        ProgressData progressData = new ProgressData(iterations);
        taskProgress.put(task.getId(), progressData);

        try {
            // Calculate Pi using the Leibniz formula with progress tracking
            double pi = calculatePi(iterations, progressData);

            // Create a detailed result
            String resultText = String.format("Calculated Pi to %d iterations. Result: %.10f",
                    iterations, pi);

            return new TaskResult("Result for " + task.getTitle(),
                    resultText,
                    LocalDateTime.now());
        } finally {
            // Ensure progress is set to 100% when done
            progressData.setCurrentIteration(iterations);
            progressData.setCurrentValue(progressData.getFinalValue());
        }
    }

    @Override
    public String getName() {
        return "Calculate Pi";
    }

    @Override
    public String getDescription() {
        return "Calculates the value of Pi using the Leibniz formula. " +
                "You can specify the number of iterations using 'iterations=X' in the task description. " +
                "This task supports progress tracking during execution.";
    }

    /**
     * Calculates Pi using the Leibniz formula: Pi/4 = 1 - 1/3 + 1/5 - 1/7 + ...
     * with progress tracking.
     *
     * @param iterations   The number of iterations to perform
     * @param progressData The object to track progress in
     * @return The calculated value of Pi
     */
    private double calculatePi(int iterations, ProgressData progressData) {
        double sum = 0.0;

        // Add a slight delay to make progress tracking more observable
        final int progressUpdateFrequency = Math.max(1, iterations / 100);

        for (int i = 0; i < iterations; i++) {
            int term = 2 * i + 1;
            if (i % 2 == 0) {
                sum += 1.0 / term;
            } else {
                sum -= 1.0 / term;
            }

            // Update progress every N iterations
            if (i % progressUpdateFrequency == 0) {
                double currentPi = 4 * sum;
                progressData.setCurrentIteration(i + 1);
                progressData.setCurrentValue(currentPi);

                // Add small delay to simulate longer-running task
                if (iterations >= 100) {
                    try {
                        System.out.println("sleeping...");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        double result = 4 * sum;
        progressData.setFinalValue(result);
        return result;
    }

    /**
     * Class to track the progress of a Pi calculation.
     */
    public static class ProgressData {
        private final int totalIterations;
        private final AtomicInteger currentIteration;
        @Getter
        private final LocalDateTime startTime;
        @Getter
        @Setter
        private volatile double currentValue;
        @Setter
        @Getter
        private volatile double finalValue;

        public ProgressData(int totalIterations) {
            this.totalIterations = totalIterations;
            this.currentIteration = new AtomicInteger(0);
            this.currentValue = 0.0;
            this.startTime = LocalDateTime.now();
        }

        public int getTotalIterations() {
            return totalIterations;
        }

        public int getCurrentIteration() {
            return currentIteration.get();
        }

        public void setCurrentIteration(int currentIteration) {
            this.currentIteration.set(currentIteration);
        }

        public int getProgressPercentage() {
            return (int) (((double) currentIteration.get() / totalIterations) * 100);
        }

        public long getElapsedTimeMillis() {
            return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
        }

        public long getEstimatedTimeRemainingMillis() {
            long elapsed = getElapsedTimeMillis();
            int progress = getProgressPercentage();

            if (progress == 0) {
                return -1; // Cannot estimate yet
            }

            return (elapsed * (100 - progress)) / progress;
        }
    }
}