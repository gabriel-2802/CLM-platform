package clm.demo.exceptions.exceptions;

/**
 * Thrown when a signed document is not available (e.g., contract not yet signed).
 */
public class SignedDocumentNotAvailableException extends RuntimeException {
    public SignedDocumentNotAvailableException(String message) {
        super(message);
    }

    public SignedDocumentNotAvailableException(Long contractId) {
        super("Signed document not yet available for contract: " + contractId);
    }
}

