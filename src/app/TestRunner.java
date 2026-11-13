package app;

import api.ApiResponse;
import api.TaskApi;
import model.TaskQuery;
import model.TaskRequest;
import parser.LlmClient;
import repository.InMemoryTaskRepository;
import service.TaskService;

public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    // Fake LLM Client jo call count track karta hai aur fixed valid JSON deta hai
    static class FakeLlmClient extends LlmClient {
        int callCount = 0;

        @Override
        public String complete(String prompt) {
            callCount++;
            if (prompt.contains("Break down")) {
                return "Step One|First step details\nStep Two|Second step details";
            }
            return "{\"title\":\"Pay Rent\",\"description\":\"Monthly rent payment\",\"dueDate\":\"2026-12-01\",\"recurrence\":\"MONTHLY\"}";
        }
    }

    // Faulty LLM Client jo galat data (khali title aur impossible date) deta hai
    static class FaultyLlmClient extends LlmClient {
        @Override
        public String complete(String prompt) {
            return "{\"title\":\"\",\"description\":\"Bad output\",\"dueDate\":\"2026-02-30\",\"recurrence\":null}";
        }
    }

    private static void check(String testName, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.out.println("[FAIL] " + testName);
            failed++;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Running Offline Test Suite ===");
        InMemoryTaskRepository repo = InMemoryTaskRepository.getInstance();
        TaskService service = new TaskService(repo);
        FakeLlmClient fakeClient = new FakeLlmClient();
        TaskApi api = new TaskApi(service, true, fakeClient);

        // 1. Valid Task Creation
        ApiResponse r1 = api.createTask(new TaskRequest("Test 1", "Desc 1", "false", "2026-12-10", "LOW"), "t-key-1");
        check("Create valid task returns 201", r1.getStatusCode() == 201);

        // 2. Invalid Input & Impossible Date (400)
        ApiResponse r2 = api.createTask(new TaskRequest("", "", "invalid", "2026-02-30", "NONE"), null);
        check("Invalid input returns 400", r2.getStatusCode() == 400 && r2.getBody().contains("VALIDATION_ERROR"));

        // 3. Unknown ID (404)
        ApiResponse r3 = api.getTask(9999);
        check("Unknown ID returns 404", r3.getStatusCode() == 404);

        // 4. Idempotency Same Key + Different Request (422)
        ApiResponse r4 = api.createTask(new TaskRequest("Changed", "Desc 1", "false", "2026-12-10", "LOW"), "t-key-1");
        check("Idempotency conflict returns 422", r4.getStatusCode() == 422);

        // 5. Subtask of Subtask (400)
        ApiResponse sub1 = api.createSubtask(1, new TaskRequest("Sub 1", "Sub desc", "false", null, "MEDIUM"));
        check("Create subtask returns 201", sub1.getStatusCode() == 201);
        ApiResponse subOfSub = api.createSubtask(2, new TaskRequest("Nested", "Fail", "false", null, "MEDIUM"));
        check("Subtask of subtask returns 400", subOfSub.getStatusCode() == 400);

        // 6. Pagination Beyond Last Page
        ApiResponse emptyPage = api.queryTasks(new TaskQuery.Builder().page(50).pageSize(10).build());
        check("Page beyond last page returns empty array", emptyPage.getStatusCode() == 200 && emptyPage.getBody().equals("[]"));

        // 7. Missing LLM_API_KEY returns 503
        if (System.getenv("LLM_API_KEY") == null || System.getenv("LLM_API_KEY").isBlank()) {
            ApiResponse missingKeyRes = api.createTaskFromText("Buy milk", "llm-k1");
            check("Missing LLM_API_KEY returns 503", missingKeyRes.getStatusCode() == 503);
        }

        // 8. Fake LLM Idempotency & Faulty LLM Output (502) (jab dummy LLM_API_KEY set ho)
        if (System.getenv("LLM_API_KEY") != null && !System.getenv("LLM_API_KEY").isBlank()) {
            api.createTaskFromText("Pay rent on 1st", "fake-llm-key");
            api.createTaskFromText("Pay rent on 1st", "fake-llm-key");
            check("Repeated idempotency key never calls FakeLlmClient twice", fakeClient.callCount == 1);

            api.setLlmConfig(true, new FaultyLlmClient());
            ApiResponse faultyRes = api.createTaskFromText("Bad AI task", "llm-bad");
            check("Faulty LLM output returns 502", faultyRes.getStatusCode() == 502);
        }

        System.out.println("\nSummary: " + passed + " PASSED, " + failed + " FAILED");
    }
}
