package service;

import exception.ValidationException;
import exception.ValidationException.FieldIssue;
import model.TaskRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TaskValidator {
    private static final DateTimeFormatter STRICT_DATE =
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final Set<String> VALID_PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");

    public static void validate(TaskRequest req) {
        List<FieldIssue> issues = new ArrayList<>();

        if (req.title == null || req.title.isBlank()) {
            issues.add(new FieldIssue("title", "must not be empty"));
        }
        if (req.description == null || req.description.isBlank()) {
            issues.add(new FieldIssue("description", "must not be empty"));
        }
        if (req.completed == null || (!req.completed.equals("true") && !req.completed.equals("false"))) {
            issues.add(new FieldIssue("completed", "must be true or false"));
        }
        if (req.dueDate != null && !req.dueDate.isBlank()) {
            try {
                LocalDate.parse(req.dueDate, STRICT_DATE);
            } catch (DateTimeParseException e) {
                issues.add(new FieldIssue("dueDate", "must be a valid YYYY-MM-DD date"));
            }
        }
        if (req.priority != null && !req.priority.isBlank() && !VALID_PRIORITIES.contains(req.priority)) {
            issues.add(new FieldIssue("priority", "must be LOW, MEDIUM, or HIGH"));
        }

        if (!issues.isEmpty()) {
            throw new ValidationException(issues);
        }
    }
}

