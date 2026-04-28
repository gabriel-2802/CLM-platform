package clm.demo.exceptions;

public class ContractGenerationFailException extends RuntimeException {
    public ContractGenerationFailException(String message) {
        super(message);
    }
}
