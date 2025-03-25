package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for notifications using JPA.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Finds all notifications for a specific user.
     *
     * @param userId The user ID
     * @return List of notifications for the user
     */
    List<Notification> findByUserId(Integer userId);

    /**
     * Finds all notifications for a specific user with a certain read status.
     *
     * @param userId The user ID
     * @param isRead Whether the notification has been read
     * @return List of notifications for the user with the specified read status
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND " +
            "((:isRead = true AND n.status IN (de.vfh.paf.tasklist.domain.model.NotificationStatus.READ, de.vfh.paf.tasklist.domain.model.NotificationStatus.ARCHIVED)) OR " +
            "(:isRead = false AND n.status NOT IN (de.vfh.paf.tasklist.domain.model.NotificationStatus.READ, de.vfh.paf.tasklist.domain.model.NotificationStatus.ARCHIVED))) " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndReadStatus(@Param("userId") Integer userId, @Param("isRead") boolean isRead);

    /**
     * Finds existing notifications by type, userId, and relatedTaskId.
     *
     * @param type          The notification type
     * @param userId        The user ID
     * @param relatedTaskId The related task ID (can be null)
     * @return List of matching notifications
     */
    List<Notification> findByTypeAndUserIdAndRelatedTaskId(String type, Integer userId, Integer relatedTaskId);

    /**
     * Deletes notifications matching specific criteria.
     *
     * @param type          The notification type
     * @param userId        The user ID
     * @param relatedTaskId The related task ID (can be null)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.type = :type AND n.userId = :userId AND " +
            "(:relatedTaskId IS NULL AND n.relatedTaskId IS NULL OR n.relatedTaskId = :relatedTaskId)")
    void deleteByTypeAndUserIdAndRelatedTaskId(
            @Param("type") String type,
            @Param("userId") Integer userId,
            @Param("relatedTaskId") Integer relatedTaskId);
}