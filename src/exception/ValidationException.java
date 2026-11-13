package exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends ApiException {
    public static class FieldIssue {
        public final String field;
        public final String issue;
        public FieldIssue(String field, String issue) {
            this.field = field;
            this.issue = issue;
        }
    }

    private final List<FieldIssue> details;

    public ValidationException(List<FieldIssue> details) {
        super(400, "VALIDATION_ERROR", "Validation failed");
        this.details = new ArrayList<>(details);
    }

    public List<FieldIssue> getDetails() { return details; }
}
