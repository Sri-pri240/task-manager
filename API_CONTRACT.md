# Task Manager API Contract

## Standard Error Shape
All error responses (`400`, `404`, `422`, `500`, `502`, `503`) follow this exact JSON structure:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable summary",
    "details": [{ "field": "fieldName", "issue": "specific issue" }]
  }
}
```

## Operations

| Operation | Parameters | Success Status | Error Statuses |
| :--- | :--- | :--- | :--- |
| `createTask` | `TaskRequest req, String idempotencyKey` | `201` | `400`, `422`, `500` |
| `createTaskFromText` | `String text, String idempotencyKey` | `201` | `422`, `502`, `503`, `500` |
| `getTask` | `int id` | `200` | `404`, `500` |
| `updateTask` | `int id, TaskRequest req` | `200` | `400`, `404`, `500` |
| `deleteTask` | `int id` | `200` | `404`, `500` |
| `createSubtask` | `int parentId, TaskRequest req` | `201` | `400`, `404`, `500` |
| `listSubtasks` | `int parentId` | `200` | `404`, `500` |
| `breakDownTask` | `int id, String idempotencyKey` | `201` | `400`, `404`, `422`, `502`, `503`, `500` |
| `queryTasks` | `TaskQuery query` | `200` | `500` |
