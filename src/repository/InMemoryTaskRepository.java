package repository;

import model.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryTaskRepository implements TaskRepository {
    private static InMemoryTaskRepository instance;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private int currentId = 1;

    private InMemoryTaskRepository() {}

    public static synchronized InMemoryTaskRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryTaskRepository();
        }
        return instance;
    }

    public synchronized int nextId() {
        return currentId++;
    }

    @Override
    public synchronized Task save(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public synchronized Optional<Task> findById(int id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public synchronized List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public synchronized boolean delete(int id) {
        return tasks.remove(id) != null;
    }
}
