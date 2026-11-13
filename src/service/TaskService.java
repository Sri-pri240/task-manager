package service;

import event.TaskEventListener;
import exception.BadGatewayException;
import exception.NotFoundException;
import exception.ValidationException;
import exception.ValidationException.FieldIssue;
import model.ParsedTask;
import model.Task;
import model.TaskQuery;
import model.TaskRequest;
import parser.BreakdownStrategy;
import parser.TaskParser;
import repository.InMemoryTaskRepository;
import repository.TaskRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {
    private final TaskRepository repository;
    private final List<TaskEventListener> listeners = new ArrayList<>();

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public void addListener(TaskEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TaskEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String eventType, Task task) {
        for (TaskEventListener listener : listeners) {
            listener.onEvent(eventType, task);
        }
    }

    private int nextId() {
        if (repository instanceof InMemoryTaskRepository memRepo) {
            return memRepo.nextId();
        }
        return repository.findAll().size() + 1;
    }

    public Task createTask(TaskRequest req) {
        TaskValidator.validate(req);
        String priority = (req.priority == null || req.priority.isBlank()) ? "MEDIUM" : req.priority;
        Task task = new Task.Builder(req.title.trim(), req.description.trim(), Boolean.parseBoolean(req.completed))
                .id(nextId())
                .dueDate(req.dueDate != null && !req.dueDate.isBlank() ? req.dueDate : null)
                .priority(priority)
                .recurrence(req.recurrence)
                .build();
        repository.save(task);
        notifyListeners("CREATED", task);
        return task;
    }

    public Task getTaskById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
    }

    public Task updateTask(int id, TaskRequest req) {
        Task existing = getTaskById(id);
        TaskValidator.validate(req);
        boolean wasCompleted = existing.isCompleted();
        boolean nowCompleted = Boolean.parseBoolean(req.completed);
        String priority = (req.priority == null || req.priority.isBlank()) ? existing.getPriority() : req.priority;

        Task updated = new Task.Builder(req.title.trim(), req.description.trim(), nowCompleted)
                .id(existing.getId())
                .dueDate(req.dueDate != null && !req.dueDate.isBlank() ? req.dueDate : null)
                .priority(priority)
                .recurrence(req.recurrence != null ? req.recurrence : existing.getRecurrence())
                .parentId(existing.getParentId())
                .build();
        repository.save(updated);
        notifyListeners("UPDATED", updated);
        if (!wasCompleted && nowCompleted) {
            notifyListeners("COMPLETED", updated);
        }
        return updated;
    }

    public void deleteTask(int id) {
        Task existing = getTaskById(id);
        for (Task t : repository.findAll()) {
            if (t.getParentId() != null && t.getParentId() == id) {
                repository.delete(t.getId());
                notifyListeners("DELETED", t);
            }
        }
        repository.delete(id);
        notifyListeners("DELETED", existing);
    }

    public Task createTaskFromText(String text, TaskParser parser) {
        String today = LocalDate.now().toString();
        ParsedTask parsed = parser.parse(text, today);
        TaskRequest req = new TaskRequest(parsed.title, parsed.description, "false", parsed.dueDate, "MEDIUM");
        req.recurrence = parsed.recurrence;
        try {
            TaskValidator.validate(req);
        } catch (ValidationException e) {
            throw new BadGatewayException("Parser returned invalid task data");
        }
        return createTask(req);
    }

    public Task createSubtask(int parentId, TaskRequest req) {
        Task parent = getTaskById(parentId);
        if (parent.getParentId() != null) {
            throw new ValidationException(List.of(new FieldIssue("parentId", "cannot add subtask to a subtask")));
        }
        TaskValidator.validate(req);
        String priority = (req.priority == null || req.priority.isBlank()) ? "MEDIUM" : req.priority;
        Task subtask = new Task.Builder(req.title.trim(), req.description.trim(), Boolean.parseBoolean(req.completed))
                .id(nextId())
                .dueDate(req.dueDate != null && !req.dueDate.isBlank() ? req.dueDate : null)
                .priority(priority)
                .parentId(parentId)
                .build();
        repository.save(subtask);
        notifyListeners("CREATED", subtask);
        return subtask;
    }

    public List<Task> listSubtasks(int parentId) {
        getTaskById(parentId);
        return repository.findAll().stream()
                .filter(t -> t.getParentId() != null && t.getParentId() == parentId)
                .collect(Collectors.toList());
    }

    public List<Task> breakDownTask(int id, BreakdownStrategy strategy) {
        Task parent = getTaskById(id);
        if (parent.getParentId() != null) {
            throw new ValidationException(List.of(new FieldIssue("id", "cannot break down a subtask")));
        }
        List<TaskRequest> requests = strategy.breakDown(parent);
        try {
            for (TaskRequest req : requests) {
                TaskValidator.validate(req);
            }
        } catch (ValidationException e) {
            throw new BadGatewayException("Breakdown strategy produced invalid subtask");
        }
        List<Task> created = new ArrayList<>();
        for (TaskRequest req : requests) {
            created.add(createSubtask(id, req));
        }
        return created;
    }

    public List<Task> queryTasks(TaskQuery query) {
        List<Task> result = new ArrayList<>(repository.findAll());
        for (TaskFilter filter : query.getFilters()) {
            result = result.stream().filter(filter::matches).collect(Collectors.toList());
        }
        if (query.getSortStrategy() != null) {
            result.sort(query.getSortStrategy().getComparator());
        }
        int fromIndex = (query.getPage() - 1) * query.getPageSize();
        if (fromIndex >= result.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + query.getPageSize(), result.size());
        return result.subList(fromIndex, toIndex);
    }
}
