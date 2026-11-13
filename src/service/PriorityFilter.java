package service;

import model.Task;

public class PriorityFilter implements TaskFilter {
    private final String priority;

    public PriorityFilter(String priority) {
        this.priority = priority;
    }

    @Override
    public boolean matches(Task task) {
        return priority != null && priority.equalsIgnoreCase(task.getPriority());
    }
}
