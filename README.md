# AI-Native Task Manager Service (Plain Java)

A REST-style task management service written in plain Java 17+ without external frameworks. It supports structured task creation from natural language using an LLM (or offline rule-based fallback), subtask breakdowns, idempotent retries, strict validation, filtering/sorting/pagination, and event listeners.

## Package Structure
- `model`: Immutable `Task` (with Builder), `TaskRequest`, `ParsedTask`, and `TaskQuery` (with Builder).
- `repository`: `TaskRepository` interface, `InMemoryTaskRepository` (Singleton), and thread-safe `IdempotencyStore` (Singleton).
- `service`: `TaskService`, `TaskValidator`, and Open/Closed `TaskFilter` & `SortStrategy` implementations.
- `parser`: `TaskParser` & `BreakdownStrategy` interfaces, LLM & Rule-Based implementations, and `ParserFactory`.
- `event`: `TaskEventListener` interface with `ConsoleLogListener` and `AuditTrailListener`.
- `exception`: Custom `ApiException` hierarchy (`400`, `404`, `422`, `502`, `503`).
- `api`: `TaskApi`, `ApiResponse`, and `ErrorResponseFactory`.
- `app`: `Main` demo runner and offline `TestRunner`.

## Setup & Running
- **Requirements**: JDK 17 or newer (tested on JDK 22).
- **Environment Variable**: Set `LLM_API_KEY` before running in LLM mode (`export LLM_API_KEY=your_key` or via IDE Run Configurations). If missing, LLM endpoints safely return `503 Service Unavailable`.
- **Compile & Run from Terminal**:
  ```bash
  javac -d out src/*/*.java
  java -cp out app.Main
  java -cp out app.TestRunner
  ```

## TaskApi Operations & Examples

| Method | Parameters | Success | Errors |
| :--- | :--- | :--- | :--- |
| `createTask` | `(TaskRequest, String idempotencyKey)` | `201` | `400, 422, 500` |
| `createTaskFromText` | `(String text, String idempotencyKey)` | `201` | `422, 502, 503, 500` |
| `getTask` | `(int id)` | `200` | `404, 500` |
| `updateTask` | `(int id, TaskRequest)` | `200` | `400, 404, 500` |
| `deleteTask` | `(int id)` | `200` | `404, 500` |
| `createSubtask` | `(int parentId, TaskRequest)` | `201` | `400, 404, 500` |
| `listSubtasks` | `(int parentId)` | `200` | `404, 500` |
| `breakDownTask` | `(int id, String idempotencyKey)` | `201` | `400, 404, 422, 502, 503, 500` |
| `queryTasks` | `(TaskQuery)` | `200` | `500` |

### Sample Responses
- **Create Task (`201`)**: `{"status":201,"body":{"id":1,"title":"Launch App","description":"Prepare release","completed":false,"dueDate":"2026-12-15","priority":"HIGH","recurrence":null,"parentId":null}}`
- **Validation Error (`400`)**: `{"status":400,"body":{"error":{"code":"VALIDATION_ERROR","message":"Validation failed","details":[{"field":"dueDate","issue":"must be a valid YYYY-MM-DD date"}]}}}`

## Design Patterns Used
1. **Builder**: `Task.Builder` and `TaskQuery.Builder` for immutable, flexible object construction.
2. **Singleton**: `InMemoryTaskRepository` and `IdempotencyStore` for centralized thread-safe state.
3. **Factory**: `ParserFactory` (selects LLM vs. Rule-Based strategy) and `ErrorResponseFactory` (builds consistent error JSON).
4. **Strategy**: `TaskParser`, `BreakdownStrategy`, `TaskFilter`, and `SortStrategy` for Open/Closed extensibility.
5. **Observer**: `TaskService` notifies `ConsoleLogListener` and `AuditTrailListener` on task lifecycle events.

## LLM Prompts & Rationale
- **Task Parsing Prompt**: Includes today's date (`YYYY-MM-DD`) so relative phrases like `"tomorrow"` or `"1st of every month"` resolve accurately, and instructs the model to reply strictly with raw JSON keys (`title`, `description`, `dueDate`, `recurrence`).
- **Breakdown Prompt**: Instructs the model to return one subtask per line in `title|description` format for deterministic parsing and validation before saving.

## AI Test Critique & Improvements
Initial AI-generated tests only covered happy paths. Additional edge cases were added in `TestRunner` for impossible calendar dates (`2026-02-30`), idempotency key payload conflicts (`422`), nested subtasks (`400`), pagination beyond the final page, and `FaultyLlmClient` bad output (`502`).
