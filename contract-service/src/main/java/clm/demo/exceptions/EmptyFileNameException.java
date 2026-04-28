package clm.demo.exceptions;

public class EmptyFileNameException extends RuntimeException {
    public EmptyFileNameException(String message) {
        super(message);
    }

    public EmptyFileNameException() {
        super("File name cannot be empty");
    }
}
