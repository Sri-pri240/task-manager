package api;

import exception.ApiException;
import exception.ValidationException;
import java.util.List;

public class ErrorResponseFactory {
    public static ApiResponse from(ApiException e) {
        StringBuilder detailsJson = new StringBuilder("[");
        if (e instanceof ValidationException ve) {
            List<ValidationException.FieldIssue> issues = ve.getDetails();
            for (int i = 0; i < issues.size(); i++) {
                ValidationException.FieldIssue issue = issues.get(i);
                detailsJson.append(String.format("{\"field\":\"%s\",\"issue\":\"%s\"}", issue.field, issue.issue));
                if (i < issues.size() - 1) detailsJson.append(",");
            }
        }
        detailsJson.append("]");

        String body = String.format(
            "{\"error\":{\"code\":\"%s\",\"message\":\"%s\",\"details\":%s}}",
            e.getErrorCode(), e.getMessage().replace("\"", "\\\""), detailsJson
        );
        return new ApiResponse(e.getStatusCode(), body);
    }

    public static ApiResponse internalError(Exception e) {
        String msg = (e.getMessage() != null) ? e.getMessage().replace("\"", "\\\"") : "Unexpected error";
        String body = String.format("{\"error\":{\"code\":\"INTERNAL_ERROR\",\"message\":\"%s\",\"details\":[]}}", msg);
        return new ApiResponse(500, body);
    }
}
