package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.Notification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Repository for notifications.
 * For this implementation, we use an in-memory storage.
 */
@Repository
public class NotificationRepository {
    private final Map<Integer, Notification> notifications = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    
    /**
     * Gets the next available ID.
     *
     * @return The next ID
     */
    public int getNextId() {
        return idGenerator.getAndIncrement();
    }
    
    /**
     * Saves a notification.
     *
     * @param notification The notification to save
     * @return The saved notification
     */
    public Notification save(Notification notification) {
        notifications.put(notification.getId(), notification);
        return notification;
    }
    
    /**
     * Finds a notification by ID.
     *
     * @param id The ID of the notification
     * @return Optional containing the notification, or empty if not found
     */
    public Optional<Notification> findById(int id) {
        return Optional.ofNullable(notifications.get(id));
    }
    
    /**
     * Gets all notifications.
     *
     * @return List of all notifications
     */
    public List<Notification> findAll() {
        return new ArrayList<>(notifications.values());
    }
}