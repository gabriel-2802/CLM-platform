package clm.user.demo.exceptions;

public class DuplicateEmailException extends RuntimeException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public DuplicateEmailException(String email) {
        super("Email already registered: " + email);
    }
}