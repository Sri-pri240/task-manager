package api;

import exception.ApiException;
import model.Task;
import model.TaskQuery;
import model.TaskRequest;
import parser.BreakdownStrategy;
import parser.LlmClient;
import parser.ParserFactory;
import parser.TaskParser;
import repository.IdempotencyStore;
import service.TaskService;

import java.util.List;
import java.util.stream.Collectors;

public class TaskApi {
    private final TaskService service;
    private final IdempotencyStore idempotencyStore = IdempotencyStore.getInstance();
    private LlmClient llmClient;
    private boolean useLlm;

    public TaskApi(TaskService service, boolean useLlm, LlmClient llmClient) {
        this.service = service;
        this.useLlm = useLlm;
        this.llmClient = llmClient;
    }

    public void setLlmConfig(boolean useLlm, LlmClient llmClient) {
        this.useLlm = useLlm;
        this.llmClient = llmClient;
    }

    private String toJsonArray(List<Task> tasks) {
        return "[" + tasks.stream().map(Task::toJson).collect(Collectors.joining(",")) + "]";
    }

    public ApiResponse createTask(TaskRequest req, String idempotencyKey) {
        try {
            String fingerprint = "CREATE|" + req.title + "|" + req.description + "|" + req.completed + "|" + req.dueDate + "|" + req.priority;
            ApiResponse cached = idempotencyStore.check(idempotencyKey, fingerprint);
            if (cached != null) return cached;

            Task created = service.createTask(req);
            ApiResponse res = new ApiResponse(201, created.toJson());
            idempotencyStore.save(idempotencyKey, fingerprint, res);
            return res;
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }

    public ApiResponse createTaskFromText(String text, String idempotencyKey) {
        try {
            String fingerprint = "TEXT|" + text;
            ApiResponse cached = idempotencyStore.check(idempotencyKey, fingerprint);
            if (cached != null) return cached;

            TaskParser parser = ParserFactory.getTaskParser(useLlm, llmClient);
            Task created = service.createTaskFromText(text, parser);
            ApiResponse res = new ApiResponse(201, created.toJson());
            idempotencyStore.save(idempotencyKey, fingerprint, res);
            return res;
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }

    public ApiResponse getTask(int id) {
        try {
            return new ApiResponse(200, service.getTaskById(id).toJson());
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }

    public ApiResponse updateTask(int id, TaskRequest req) {
        try {
            return new ApiResponse(200, service.updateTask(id, req).toJson());
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }

    public ApiResponse deleteTask(int id) {
        try {
            service.deleteTask(id);
            return new ApiResponse(200, "{\"deleted\":true,\"id\":" + id + "}");
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }

    public ApiResponse createSubtask(int parentId, TaskRequest req) {
        try {
            return new ApiResponse(201, service.createSubtask(parentId, req).toJson());
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }

    public ApiResponse listSubtasks(int parentId) {
        try {
            return new ApiResponse(200, toJsonArray(service.listSubtasks(parentId)));
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }

    public ApiResponse breakDownTask(int id, String idempotencyKey) {
        try {
            String fingerprint = "BREAKDOWN|" + id;
            ApiResponse cached = idempotencyStore.check(idempotencyKey, fingerprint);
            if (cached != null) return cached;

            BreakdownStrategy strategy = ParserFactory.getBreakdownStrategy(useLlm, llmClient);
            List<Task> subtasks = service.breakDownTask(id, strategy);
            ApiResponse res = new ApiResponse(201, toJsonArray(subtasks));
            idempotencyStore.save(idempotencyKey, fingerprint, res);
            return res;
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }

    public ApiResponse queryTasks(TaskQuery query) {
        try {
            return new ApiResponse(200, toJsonArray(service.queryTasks(query)));
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internalError(e);
        }
    }
}
