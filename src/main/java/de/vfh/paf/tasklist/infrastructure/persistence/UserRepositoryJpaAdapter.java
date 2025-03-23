package de.vfh.paf.tasklist.infrastructure.persistence;

import de.vfh.paf.tasklist.domain.model.User;
import de.vfh.paf.tasklist.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory cache adapter for UserRepository to maintain compatibility with
 * the existing code while also persisting to the database.
 * This acts as a bridge between the database and the in-memory references.
 */
@Component
@Primary
public class UserRepositoryJpaAdapter {

    private final UserRepository userRepository;
    private final Map<Integer, User> userCache = new ConcurrentHashMap<>();
    private final AtomicInteger userIdGenerator = new AtomicInteger(1);

    @Autowired
    public UserRepositoryJpaAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Preload the cache with users from the database
        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            userCache.put(user.getId(), user);
            // Update the ID generator if necessary
            if (user.getId() >= userIdGenerator.get()) {
                userIdGenerator.set(user.getId() + 1);
            }
        }
    }

    /**
     * Saves a user.
     *
     * @param user The user to save
     * @return The saved user
     */
    public User save(User user) {
        if (user.getId() == null) {
            // New user, assign ID
            user.setId(userIdGenerator.getAndIncrement());
        }

        // Save to database
        User savedUser = userRepository.save(user);

        // Update cache
        userCache.put(savedUser.getId(), savedUser);

        return savedUser;
    }

    /**
     * Finds a user by ID.
     *
     * @param id The ID of the user
     * @return Optional containing the user, or empty if not found
     */
    public Optional<User> findById(int id) {
        // Check cache first
        User user = userCache.get(id);
        if (user != null) {
            return Optional.of(user);
        }

        // Then check database
        Optional<User> userOpt = userRepository.findById(id);

        // Update cache if found
        userOpt.ifPresent(u -> userCache.put(id, u));

        return userOpt;
    }

    /**
     * Gets all users.
     *
     * @return List of all users
     */
    public List<User> findAll() {
        // Ensure the database and cache are in sync
        List<User> usersFromDb = userRepository.findAll();
        for (User user : usersFromDb) {
            if (!userCache.containsKey(user.getId())) {
                userCache.put(user.getId(), user);
            }
        }
        return new ArrayList<>(userCache.values());
    }
}