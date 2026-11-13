package exception;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(422, "CONFLICT", message);
    }
}

