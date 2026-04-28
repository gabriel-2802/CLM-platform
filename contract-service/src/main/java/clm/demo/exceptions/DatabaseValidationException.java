package clm.demo.exceptions;

/**
 * Exception thrown when database validation constraints are violated.
 * This can occur when Hibernate/JPA attempts to persist invalid data.
 */
public class DatabaseValidationException extends RuntimeException {
    private String constraintName;
    private String details;

    public DatabaseValidationException(String message) {
        super(message);
    }

    public DatabaseValidationException(String message, String constraintName) {
        super(message);
        this.constraintName = constraintName;
    }

    public DatabaseValidationException(String message, String constraintName, String details) {
        super(message);
        this.constraintName = constraintName;
        this.details = details;
    }

    public DatabaseValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseValidationException(String message, String constraintName, Throwable cause) {
        super(message, cause);
        this.constraintName = constraintName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getDetails() {
        return details;
    }
}

