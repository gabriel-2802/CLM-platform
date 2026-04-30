package clm.demo.exceptions.exceptions;

import lombok.Getter;
import java.util.List;

/**
 * Exception thrown when required field mappings are missing for contract generation.
 * Contains details about which fields are missing values.
 */
@Getter
public class MissingMandatoryFieldException extends RuntimeException {
    private final List<String> missingFields;

    public MissingMandatoryFieldException(String message, List<String> missingFields) {
        super(message);
        this.missingFields = missingFields;
    }
}

