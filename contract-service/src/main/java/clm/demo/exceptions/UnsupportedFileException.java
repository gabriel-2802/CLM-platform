package clm.demo.exceptions;

public class UnsupportedFileException extends RuntimeException {
    public UnsupportedFileException(String message) {
        super(message);
    }

    public UnsupportedFileException() {
        super("Unsupported file type. Only .docx and .pdf files are allowed.");
    }
}
