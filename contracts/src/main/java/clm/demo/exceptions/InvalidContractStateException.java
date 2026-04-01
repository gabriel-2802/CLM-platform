package clm.demo.exceptions;

public class InvalidContractStateException extends RuntimeException {
    public InvalidContractStateException(String message) {
        super(message);
    }
}
