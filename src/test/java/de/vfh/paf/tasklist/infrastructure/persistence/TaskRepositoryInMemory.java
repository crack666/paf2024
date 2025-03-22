package de.vfh.paf.tasklist.infrastructure.persistence;

import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * In-memory implementation of TaskRepository for testing purposes.
 */
public class TaskRepositoryInMemory implements TaskRepository {
    private final Map<Integer, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public List<Task> findAllByAssignedUserId(Integer userId) {
        return tasks.values().stream()
                .filter(task -> Objects.equals(task.getAssignedUserId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findTasksByDependency(Integer taskId) {
        return tasks.values().stream()
                .filter(task -> task.getDependencies().stream()
                        .anyMatch(dep -> Objects.equals(dep.getId(), taskId)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findOverdueTasks(LocalDateTime currentTime) {
        return tasks.values().stream()
                .filter(task -> !task.isCompleted() && task.getDueDate() != null && task.getDueDate().isBefore(currentTime))
                .collect(Collectors.toList());
    }

    @Override
    public <S extends Task> S save(S task) {
        if (task.getId() == null) {
            task.setId(idGenerator.getAndIncrement());
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public <S extends Task> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<Task> findById(Integer id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public boolean existsById(Integer id) {
        return tasks.containsKey(id);
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Task> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<Task> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<Task> findAllById(Iterable<Integer> ids) {
        List<Task> result = new ArrayList<>();
        for (Integer id : ids) {
            findById(id).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public long count() {
        return tasks.size();
    }

    @Override
    public void deleteById(Integer id) {
        tasks.remove(id);
    }

    @Override
    public void delete(Task entity) {
        if (entity.getId() != null) {
            tasks.remove(entity.getId());
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Integer> ids) {
        for (Integer id : ids) {
            tasks.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Task> entities) {
        for (Task entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        tasks.clear();
    }

    @Override
    public void flush() {
        // No-op in an in-memory implementation
    }

    @Override
    public <S extends Task> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends Task> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<Task> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> ids) {
        deleteAllById(ids);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public Task getOne(Integer id) {
        return findById(id).orElse(null);
    }

    @Override
    public Task getById(Integer id) {
        return findById(id).orElse(null);
    }

    @Override
    public Task getReferenceById(Integer id) {
        return findById(id).orElse(null);
    }

    @Override
    public <S extends Task> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Task> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Task> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Task> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Task> long count(Example<S> example) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Task> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public <S extends Task, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Method not implemented");
    }
}