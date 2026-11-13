package model;

public class ParsedTask {
    public final String title;
    public final String description;
    public final String dueDate;
    public final String recurrence;

    public ParsedTask(String title, String description, String dueDate, String recurrence) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.recurrence = recurrence;
    }
}
