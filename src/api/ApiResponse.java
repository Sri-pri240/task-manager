package api;

public class ApiResponse {
    private final int statusCode;
    private final String body;

    public ApiResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }

    public String toJson() {
        return String.format("{\"status\":%d,\"body\":%s}", statusCode, body);
    }

    
    public String toString() {
        return toJson();
    }
}

