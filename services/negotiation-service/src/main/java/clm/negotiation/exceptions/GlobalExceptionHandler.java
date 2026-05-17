package clm.negotiation.exceptions;

import clm.negotiation.exceptions.exceptions.InvalidNegotiationStateException;
import clm.negotiation.exceptions.exceptions.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI ERR_BASE = URI.create("https://api.clm.demo/errors/");

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Resource Not Found", e.getMessage(), "resource-not-found");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException e) {
        log.warn("Endpoint not found: {} {}", e.getHttpMethod(), e.getResourcePath());
        String detail = "Endpoint '%s %s' does not exist.".formatted(e.getHttpMethod(), e.getResourcePath());
        return problem(HttpStatus.NOT_FOUND, "Endpoint Not Found", detail, "endpoint-not-found");
    }

    @ExceptionHandler(InvalidNegotiationStateException.class)
    public ProblemDetail handleInvalidState(InvalidNegotiationStateException e) {
        log.warn("Invalid negotiation state transition: {}", e.getMessage());
        return problem(HttpStatus.CONFLICT, "Invalid Negotiation State", e.getMessage(), "invalid-negotiation-state");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.warn("Validation failed: {}", errors);
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation Failed",
                String.join(", ", errors), "validation-failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Validation Failed", e.getMessage(), "validation-failed");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Invalid Request", e.getMessage(), "invalid-request");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception e) {
        log.error("Unexpected internal error", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred.", "internal-error");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, String slug) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(ERR_BASE.resolve(slug));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
