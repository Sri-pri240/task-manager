package model;

public class Task {
    private final int id;
    private final String title;
    private final String description;
    private final boolean completed;
    private final String dueDate;
    private final String priority;
    private final String recurrence;
    private final Integer parentId;

    private Task(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.completed = builder.completed;
        this.dueDate = builder.dueDate;
        this.priority = builder.priority;
        this.recurrence = builder.recurrence;
        this.parentId = builder.parentId;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public String getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public String getRecurrence() { return recurrence; }
    public Integer getParentId() { return parentId; }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"title\":\"%s\",\"description\":\"%s\",\"completed\":%b,\"dueDate\":%s,\"priority\":\"%s\",\"recurrence\":%s,\"parentId\":%s}",
            id, escape(title), escape(description), completed,
            dueDate == null ? "null" : "\"" + escape(dueDate) + "\"",
            priority,
            recurrence == null ? "null" : "\"" + escape(recurrence) + "\"",
            parentId == null ? "null" : parentId
        );
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    public static class Builder {
        private final String title;
        private final String description;
        private final boolean completed;

        private int id;
        private String dueDate;
        private String priority = "MEDIUM";
        private String recurrence;
        private Integer parentId;

        public Builder(String title, String description, boolean completed) {
            this.title = title;
            this.description = description;
            this.completed = completed;
        }

        public Builder id(int id) { this.id = id; return this; }
        public Builder dueDate(String dueDate) { this.dueDate = dueDate; return this; }
        public Builder priority(String priority) { this.priority = priority; return this; }
        public Builder recurrence(String recurrence) { this.recurrence = recurrence; return this; }
        public Builder parentId(Integer parentId) { this.parentId = parentId; return this; }

        public Task build() { return new Task(this); }
    }
}

