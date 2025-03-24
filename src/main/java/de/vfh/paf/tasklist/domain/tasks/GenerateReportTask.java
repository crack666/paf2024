package de.vfh.paf.tasklist.domain.tasks;

import de.vfh.paf.tasklist.domain.model.AbstractRunnableTask;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * A task that generates a simulated report.
 * This mimics a data processing task that might generate reports from data.
 */
public class GenerateReportTask extends AbstractRunnableTask {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected TaskResult execute(Task task) {
        // Simulate report generation work
        try {
            Thread.sleep(10000); // Simulate some processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate a dummy report
        String reportType = extractReportType(task.getDescription());
        String reportId = UUID.randomUUID().toString().substring(0, 8);

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("=== ").append(reportType).append(" Report ===\n");
        reportContent.append("Report ID: ").append(reportId).append("\n");
        reportContent.append("Generated: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        reportContent.append("Requested by User: ").append(task.getAssignedUserId()).append("\n");
        reportContent.append("\nReport Summary:\n");

        // Add sample data based on report type
        if ("Sales".equalsIgnoreCase(reportType)) {
            reportContent.append("Total Sales: $").append((int) (Math.random() * 10000)).append("\n");
            reportContent.append("New Customers: ").append((int) (Math.random() * 100)).append("\n");
            reportContent.append("Best Selling Product: Product-").append((int) (Math.random() * 10));
        } else if ("Performance".equalsIgnoreCase(reportType)) {
            reportContent.append("Average Response Time: ").append((int) (Math.random() * 500)).append("ms\n");
            reportContent.append("Server Uptime: 99.").append((int) (Math.random() * 100)).append("%\n");
            reportContent.append("Error Rate: ").append(String.format("%.2f", Math.random() * 2)).append("%");
        } else {
            reportContent.append("Summary data for ").append(reportType).append(" not available.");
        }

        return new TaskResult("Report Generation Complete",
                reportContent.toString(),
                LocalDateTime.now());
    }

    @Override
    public String getName() {
        return "Generate Report";
    }

    @Override
    public String getDescription() {
        return "Generates various types of reports. Specify the report type in the description " +
                "using 'type=X'. Available types: Sales, Performance, etc.";
    }

    /**
     * Extracts the report type from the task description.
     *
     * @param description The task description
     * @return The extracted report type, or "Generic" if not specified
     */
    private String extractReportType(String description) {
        if (description != null && description.contains("type=")) {
            try {
                String[] parts = description.split("type=");
                if (parts.length > 1) {
                    return parts[1].trim().split("\\s+")[0];
                }
            } catch (Exception e) {
                // Use default if parsing fails
            }
        }
        return "Generic";
    }
}