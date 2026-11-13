package parser;

import exception.BadGatewayException;
import model.Task;
import model.TaskRequest;
import java.util.ArrayList;
import java.util.List;

public class LlmBreakdownStrategy implements BreakdownStrategy {
    private final LlmClient client;

    public LlmBreakdownStrategy(LlmClient client) {
        this.client = client;
    }

    @Override
    public List<TaskRequest> breakDown(Task task) {
        String prompt = "Break down this task into smaller subtasks. Title: " + task.getTitle()
                + ", Description: " + task.getDescription()
                + ". Reply with one subtask per line in exact format: title|description";
        try {
            String reply = client.complete(prompt);
            List<TaskRequest> subtasks = new ArrayList<>();
            String[] lines = reply.split("\\r?\\n");
            for (String line : lines) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|", 2);
                if (parts.length < 2) {
                    throw new BadGatewayException("Malformed subtask line from LLM");
                }
                subtasks.add(new TaskRequest(parts[0].trim(), parts[1].trim(), "false", task.getDueDate(), task.getPriority()));
            }
            if (subtasks.isEmpty()) {
                throw new BadGatewayException("LLM returned no subtasks");
            }
            return subtasks;
        } catch (BadGatewayException e) {
            throw e;
        } catch (Exception e) {
            throw new BadGatewayException("LLM breakdown failed: " + e.getMessage());
        }
    }
}

