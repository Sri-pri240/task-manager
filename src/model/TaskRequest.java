package model;

public class TaskRequest {
    public String title;
    public String description;
    public String completed;
    public String dueDate;
    public String priority;
    public String recurrence;

    public TaskRequest(String title, String description, String completed, String dueDate, String priority) {
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.dueDate = dueDate;
        this.priority = priority;
    }
}

