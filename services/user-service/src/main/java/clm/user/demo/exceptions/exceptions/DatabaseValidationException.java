package clm.user.demo.exceptions.exceptions;

public class DatabaseValidationException extends RuntimeException {
    public DatabaseValidationException(String message) {
        super(message);
    }
}
