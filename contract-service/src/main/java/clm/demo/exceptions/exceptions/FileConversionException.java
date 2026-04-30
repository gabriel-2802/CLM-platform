package clm.demo.exceptions.exceptions;

/**
 * Thrown when a file conversion operation fails.
 */
public class FileConversionException extends RuntimeException {
    public FileConversionException(String message) {
        super(message);
    }

    public FileConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}

