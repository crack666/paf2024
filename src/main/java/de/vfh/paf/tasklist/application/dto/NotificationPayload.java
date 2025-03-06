package de.vfh.paf.tasklist.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payload for WebSocket notifications.
 * Contains all the necessary information for a notification to be sent via WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {
    private int notificationId;
    private String type;
    private String message;
    private String urgency;
    private Integer taskId;
    private LocalDateTime timestamp;
    private boolean read;
    
    /**
     * Factory method to create a payload from a DTO.
     * 
     * @param dto The NotificationDTO to convert
     * @return The NotificationPayload
     */
    public static NotificationPayload fromDto(NotificationDTO dto) {
        return new NotificationPayload(
            dto.getId(),
            dto.getType(),
            dto.getMessage(),
            dto.getUrgency(),
            dto.getRelatedTaskId(),
            dto.getTimestamp(),
            dto.isRead()
        );
    }
}