package app;

import api.TaskApi;
import event.AuditTrailListener;
import event.ConsoleLogListener;
import model.TaskQuery;
import model.TaskRequest;
import parser.LlmClient;
import repository.InMemoryTaskRepository;
import service.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Task Manager Started ===");

        // 1. Setup Repository, Service (Observer), and API
        InMemoryTaskRepository repo = InMemoryTaskRepository.getInstance();
        TaskService service = new TaskService(repo);

        ConsoleLogListener consoleListener = new ConsoleLogListener();
        AuditTrailListener auditListener = new AuditTrailListener();
        service.addListener(consoleListener);
        service.addListener(auditListener);

        // Start in offline rule-based mode (useLlm = false) so Main works without an API key
        TaskApi api = new TaskApi(service, false, new LlmClient());

        // 2. CRUD & Validation
        System.out.println("\n--- 1. Create Valid Task ---");
        System.out.println(api.createTask(new TaskRequest("Launch App", "Prepare release, test endpoints and deploy", "false", "2026-12-15", "HIGH"), "key-1"));

        System.out.println("\n--- 2. Validation Error (400) ---");
        System.out.println(api.createTask(new TaskRequest("", "Desc", "maybe", "2026-02-30", "URGENT"), null));

        // 3. Idempotency Check (Same key + same request = cached 201; Same key + different request = 422)
        System.out.println("\n--- 3. Idempotency Retry & Conflict (422) ---");
        System.out.println("Retry same key: " + api.createTask(new TaskRequest("Launch App", "Prepare release, test endpoints and deploy", "false", "2026-12-15", "HIGH"), "key-1"));
        System.out.println("Conflict key:   " + api.createTask(new TaskRequest("Different Title", "Different Desc", "false", "2026-12-15", "HIGH"), "key-1"));

        // 4. Natural Language Task Creation (Rule-Based)
        System.out.println("\n--- 4. Create From Text ---");
        System.out.println(api.createTaskFromText("Pay rent on the 1st of every month", "key-text-1"));

        // 5. Subtasks & Breakdown
        System.out.println("\n--- 5. Break Down Task #1 ---");
        System.out.println(api.breakDownTask(1, "key-bd-1"));
        System.out.println("Subtasks of #1: " + api.listSubtasks(1));
        System.out.println("Subtask of Subtask (400): " + api.createSubtask(3, new TaskRequest("Nested", "Should fail", "false", null, "LOW")));

        // 6. Filtering, Search, Sorting & Pagination
        System.out.println("\n--- 6. Query (Filter + Sort + Page) ---");
        TaskQuery query = new TaskQuery.Builder()
                .addFilter(new CompletedFilter(false))
                .addFilter(new KeywordSearchFilter("release"))
                .sortBy(new SortByTitle())
                .page(1)
                .pageSize(5)
                .build();
        System.out.println(api.queryTasks(query));

        // 7. Observer Removal & Cascade Delete
        System.out.println("\n--- 7. Remove ConsoleListener & Cascade Delete Task #1 ---");
        service.removeListener(consoleListener);
        System.out.println("Before delete total tasks: " + repo.findAll().size());
        System.out.println(api.deleteTask(1));
        System.out.println("After cascade delete total tasks: " + repo.findAll().size());
        System.out.println("Audit Trail Counts: " + auditListener.getCounts());
    }
}

