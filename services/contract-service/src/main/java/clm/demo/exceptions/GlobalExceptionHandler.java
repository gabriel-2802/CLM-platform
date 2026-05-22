package clm.demo.exceptions;

import clm.demo.exceptions.exceptions.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Intercepts exceptions across all controllers to provide consistent JSON error responses.
 * Prevents leaking of internal stack traces to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI ERR_BASE = URI.create("https://api.clm.demo/errors/");

    /**
     * Handles cases where a requested resource does not exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Resource Not Found", e.getMessage(), "resource-not-found");
    }

    /**
     * Handles cases where a requested endpoint or resource is not found.
     * This typically occurs when a request path does not match any defined endpoint.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException e) {
        log.warn("Endpoint not found: {} {}", e.getHttpMethod(), e.getResourcePath());
        String detail = "Endpoint '%s %s' does not exist.".formatted(e.getHttpMethod(), e.getResourcePath());
        return problem(HttpStatus.NOT_FOUND, "Endpoint Not Found", detail, "endpoint-not-found");
    }

    /**
     * Handles validation errors in request body (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        log.warn("Request validation failed: {}", errors);
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation Failed",
                String.join(", ", errors), "validation-failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    /**
     * Handles Bean Validation failures triggered by {@code @Validated} + {@code @Valid}
     * on service-layer method parameters (e.g., null required fields).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(cv -> {
                    // Extract only the leaf field name — avoids leaking service-layer internals.
                    String path = StreamSupport
                            .stream(cv.getPropertyPath().spliterator(), false)
                            .reduce((first, second) -> second)
                            .map(Object::toString)
                            .orElse(cv.getPropertyPath().toString());
                    return path + ": " + cv.getMessage();
                })
                .toList();
        log.warn("Constraint violation: {}", errors);
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation Failed",
                String.join(", ", errors), "validation-failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    /**
     * Handles method-level validation failures raised by Spring 7's HandlerMethodValidator
     * (e.g., {@code @Valid} on {@code @RequestBody} when the validator is not configured via
     * {@code setValidator}, or constraint violations on {@code @RequestParam} /
     * {@code @PathVariable} annotated with bean-validation constraints on a
     * {@code @Validated} controller).
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleHandlerMethodValidation(HandlerMethodValidationException e) {
        log.warn("Handler method validation failed: {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more request parameters failed validation.", "validation-failed");
    }

    /**
     * Handles cases where a required multipart file or form part is missing from the request.
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ProblemDetail handleMissingPart(MissingServletRequestPartException e) {
        log.warn("Required multipart part missing: {}", e.getRequestPartName());
        return problem(HttpStatus.BAD_REQUEST, "Missing Required Field",
                "Required field '%s' is missing.".formatted(e.getRequestPartName()), "missing-field");
    }

    /**
     * Handles cases where a required query or path parameter is absent from the request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("Required request parameter missing: {}", e.getParameterName());
        return problem(HttpStatus.BAD_REQUEST, "Missing Required Parameter",
                "Required parameter '%s' is missing.".formatted(e.getParameterName()), "missing-parameter");
    }

    /**
     * Handles validation errors in requests (e.g., empty files, invalid data).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Invalid Request", e.getMessage(), "invalid-request");
    }

    /**
     * Handles cases where a file is uploaded without a name or with a blank name.
     */
    @ExceptionHandler(EmptyFileNameException.class)
    public ProblemDetail handleEmptyFileName(EmptyFileNameException e) {
        log.warn("Upload rejected — empty file name: {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Template Upload Failed", e.getMessage(), "empty-filename");
    }

    /**
     * Handles cases where a template field is submitted as part of a mapping
     * request but does not belong to the specified template.
     */
    @ExceptionHandler(TemplateFieldOwnershipException.class)
    public ProblemDetail handleTemplateFieldOwnership(TemplateFieldOwnershipException e) {
        log.warn("Field ownership violation: {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Field Does Not Belong To Template",
                e.getMessage(), "field-ownership");
    }

    /**
     * Handles missing mandatory field mappings during contract generation.
     */
    @ExceptionHandler(MissingMandatoryFieldException.class)
    public ProblemDetail handleMissingMandatoryField(MissingMandatoryFieldException e) {
        log.warn("Contract generation failed — missing mandatory fields: {}", e.getMissingFields());
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Missing Required Field Mappings",
                e.getMessage(), "missing-mandatory-fields");
        pd.setProperty("missingFields", e.getMissingFields());
        return pd;
    }

    /**
     * Handles cases where an uploaded file fails validation or cannot be parsed.
     */
    @ExceptionHandler(InvalidFileException.class)
    public ProblemDetail handleInvalidFile(InvalidFileException e) {
        log.warn("Invalid file: {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Invalid File", e.getMessage(), "invalid-file");
    }

    /**
     * Handles cases where the HTTP method used is not supported for the target endpoint.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Unsupported HTTP method: {}", e.getMethod());
        String supported = Optional.ofNullable(e.getSupportedHttpMethods())
                .map(methods -> methods.stream().map(Object::toString).toList())
                .map(list -> String.join(", ", list))
                .orElse("unknown");
        String detail = "Method '%s' is not supported. Supported: %s".formatted(e.getMethod(), supported);
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", detail, "method-not-allowed");
    }

    /**
     * Handles duplicate template name attempts during template upload.
     */
    @ExceptionHandler(DuplicateTemplateNameException.class)
    public ProblemDetail handleDuplicateTemplateName(DuplicateTemplateNameException e) {
        log.warn("Template upload rejected — duplicate name: {}", e.getMessage());
        return problem(HttpStatus.CONFLICT, "Template Name Already Exists", e.getMessage(), "duplicate-name");
    }

    /**
     * Handles attempts to perform state transitions on a contract that is not
     * in the required state (e.g., terminating a non-ACTIVE contract).
     */
    @ExceptionHandler(InvalidContractStateException.class)
    public ProblemDetail handleInvalidContractState(InvalidContractStateException e) {
        log.warn("Invalid contract state transition: {}", e.getMessage());
        return problem(HttpStatus.CONFLICT, "Invalid Contract State", e.getMessage(), "invalid-contract-state");
    }

    /**
     * Handles attempts to perform an illegal state transition on an appendix
     * (e.g., uploading a signed document to an appendix that is already SIGNED).
     */
    @ExceptionHandler(InvalidAppendixStateException.class)
    public ProblemDetail handleInvalidAppendixState(InvalidAppendixStateException e) {
        log.warn("Invalid appendix state transition: {}", e.getMessage());
        return problem(HttpStatus.CONFLICT, "Invalid Appendix State", e.getMessage(), "invalid-appendix-state");
    }

    /**
     * Handles cases where a signed document is not yet available for download,
     * typically because the contract has not been signed yet.
     */
    @ExceptionHandler(SignedDocumentNotAvailableException.class)
    public ProblemDetail handleSignedDocumentNotAvailable(SignedDocumentNotAvailableException e) {
        log.warn("Signed document unavailable: {}", e.getMessage());
        return problem(HttpStatus.CONFLICT, "Signed Document Not Available",
                e.getMessage(), "signed-doc-unavailable");
    }

    /**
     * Handles cases where the uploaded file format is not PDF or DOCX.
     */
    @ExceptionHandler(UnsupportedFileException.class)
    public ProblemDetail handleUnsupportedFile(UnsupportedFileException e) {
        log.warn("Upload rejected — unsupported file type: {}", e.getMessage());
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported File Format",
                e.getMessage(), "unsupported-file");
    }

    /**
     * Handles unsupported format conversion requests (e.g., PDF to PDF).
     */
    @ExceptionHandler(UnsupportedConversionException.class)
    public ProblemDetail handleUnsupportedConversion(UnsupportedConversionException e) {
        log.warn("Format conversion not supported: {}", e.getMessage());
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Unsupported Format Conversion",
                e.getMessage(), "unsupported-conversion");
    }

    /**
     * Handles cases where a template is not fully mapped before generation.
     */
    @ExceptionHandler(TemplateIncompleteException.class)
    public ProblemDetail handleTemplateIncomplete(TemplateIncompleteException e) {
        log.warn("Contract generation rejected — template incomplete: {}", e.getMessage());
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Template Configuration Incomplete",
                e.getMessage(), "template-incomplete");
    }

    /**
     * Handles document conversion failures during DOCX ↔ PDF conversion.
     */
    @ExceptionHandler(FileConversionException.class)
    public ProblemDetail handleFileConversion(FileConversionException e) {
        log.error("Document conversion failed", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Document Conversion Failed",
                "An error occurred during document conversion.", "conversion-failed");
    }

    /**
     * Handles failures during template upload and processing.
     */
    @ExceptionHandler(TemplateUploadException.class)
    public ProblemDetail handleTemplateUpload(TemplateUploadException e) {
        log.error("Template upload failed", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Template Upload Failed",
                "An error occurred during template upload.", "upload-failed");
    }

    /**
     * Handles failures during template download or decompression.
     */
    @ExceptionHandler(TemplateDownloadException.class)
    public ProblemDetail handleTemplateDownload(TemplateDownloadException e) {
        log.error("Template download failed", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Template Download Failed",
                "An error occurred during template download.", "download-failed");
    }

    /**
     * Handles failures during contract document generation.
     */
    @ExceptionHandler(ContractGenerationFailException.class)
    public ProblemDetail handleContractGenerationFail(ContractGenerationFailException e) {
        log.error("Contract generation failed", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Contract Generation Failed",
                "An error occurred during contract generation.", "generation-failed");
    }

    /**
     * Handles database validation constraint violations (CHECK constraints, triggers, etc.).
     */
    @ExceptionHandler(DatabaseValidationException.class)
    public ProblemDetail handleDatabaseValidation(DatabaseValidationException e) {
        log.warn("Database validation failed [{}]: {}", e.getConstraintName(), e.getMessage());
        String detail = Optional.ofNullable(e.getDetails()).orElseGet(e::getMessage);
        return problem(HttpStatus.BAD_REQUEST, "Data Validation Failed", detail, "db-validation-failed");
    }

    /**
     * Handles data access exceptions caused by constraint violations or type mismatches.
     * Translates known DB constraint patterns into user-friendly messages.
     */
    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ProblemDetail handleInvalidDataAccess(InvalidDataAccessResourceUsageException e) {
        log.warn("Invalid data access: {}", e.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Data Validation Error",
                resolveDataAccessMessage(e.getMessage()), "data-access-error");
    }

    /**
     * Handles generic data access exceptions not covered by more specific handlers.
     */
    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccess(DataAccessException e) {
        log.error("Database access error", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Database Error",
                "A database error occurred. Please contact support.", "database-error");
    }

    @ExceptionHandler
    public ProblemDetail handleUpdateException(InvalidContractUpdateException e) {
        log.error("Invalid contract update", e);
        return problem(HttpStatus.BAD_REQUEST, "Invalid Contract Update",
                "The contract update is invalid. Please check the request and try again.", "invalid-contract-update");
    }

    /**
     * Catch-all for any unexpected exceptions to prevent 500 white-label error pages.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception e) {
        log.error("Unexpected internal error", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please contact support.", "internal-error");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Constructs a consistent {@link ProblemDetail} with status, title, detail, RFC type URI,
     * and a UTC timestamp property.
     */
    private ProblemDetail problem(HttpStatus status, String title, String detail, String errorSlug) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(ERR_BASE.resolve(errorSlug));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /**
     * Translates known PostgreSQL constraint violation message fragments into safe,
     * user-friendly strings. Avoids exposing raw SQL or internal DB details to the client.
     */
    private String resolveDataAccessMessage(String message) {
        if (Objects.isNull(message)) {
            return "A data validation error occurred. Please check your input.";
        }
        if (message.contains("duplicate key value violates unique constraint")) return "A record with this value already exists.";
        if (message.contains("violates foreign key constraint"))               return "Referenced record does not exist.";
        if (message.contains("violates check constraint"))                     return "Data violates a validation rule.";
        if (message.contains("violates not-null constraint"))                  return "A required field is missing.";
        if (message.contains("bytea"))                                         return "Invalid file data format.";
        return "A data validation error occurred. Please check your input.";
    }
}
