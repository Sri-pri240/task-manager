package parser;

import model.Task;
import model.TaskRequest;
import java.util.ArrayList;
import java.util.List;

public class RuleBasedBreakdownStrategy implements BreakdownStrategy {
    @Override
    public List<TaskRequest> breakDown(Task task) {
        List<TaskRequest> list = new ArrayList<>();
        String[] parts = task.getDescription().split(",|\\band\\b");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(new TaskRequest(trimmed, "Subtask: " + trimmed, "false", task.getDueDate(), task.getPriority()));
            }
        }
        return list;
    }
}
