package clm.demo.exceptions.exceptions;

/**
 * Exception thrown when attempting to create a template with a name that already exists.
 */
public class DuplicateTemplateNameException extends RuntimeException {
    public DuplicateTemplateNameException(String message) {
        super(message);
    }
}

