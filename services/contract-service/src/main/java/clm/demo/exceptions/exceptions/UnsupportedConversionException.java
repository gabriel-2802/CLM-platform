package clm.demo.exceptions.exceptions;

/**
 * Thrown when a requested format conversion is not supported.
 */
public class UnsupportedConversionException extends RuntimeException {
    public UnsupportedConversionException(String message) {
        super(message);
    }

    public UnsupportedConversionException(String sourceFormat, String targetFormat) {
        super("Unsupported conversion: " + sourceFormat + " to " + targetFormat);
    }
}

