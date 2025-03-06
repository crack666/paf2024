package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository for users.
 * For this implementation, we use an in-memory storage.
 */
@Repository
public class UserRepository {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    
    /**
     * Saves a user.
     *
     * @param user The user to save
     * @return The saved user
     */
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }
    
    /**
     * Finds a user by ID.
     *
     * @param id The ID of the user
     * @return Optional containing the user, or empty if not found
     */
    public Optional<User> findById(int id) {
        return Optional.ofNullable(users.get(id));
    }
    
    /**
     * Gets all users.
     *
     * @return List of all users
     */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}