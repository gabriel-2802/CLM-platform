package clm.demo.controllers;

import clm.demo.dto.responses.ErrorResponseDTO;
import clm.demo.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Intercepts exceptions across all controllers to provide consistent JSON error responses.
 * Prevents leaking of internal stack traces to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles cases where a requested resource does not exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("resource not found: {}", e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", e.getMessage());
    }

    /**
     * Handles validation errors in requests (e.g., empty files, invalid data).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("invalid argument provided: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request", e.getMessage());
    }

    /**
     * Handles cases where a file is uploaded without a name or with a blank name.
     */
    @ExceptionHandler(EmptyFileNameException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmptyFileNameException(EmptyFileNameException e) {
        log.warn("upload rejected — empty file name: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Template upload failed", e.getMessage());
    }

    /**
     * Handles cases where the uploaded file format is not PDF or DOCX.
     */
    @ExceptionHandler(UnsupportedFileException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnsupportedFileException(UnsupportedFileException e) {
        log.warn("upload rejected — unsupported file type: {}", e.getMessage());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported file format", e.getMessage());
    }

    /**
     * Handles unsupported format conversion requests (e.g., PDF to PDF).
     */
    @ExceptionHandler(UnsupportedConversionException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnsupportedConversionException(UnsupportedConversionException e) {
        log.warn("format conversion not supported: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Unsupported format conversion", e.getMessage());
    }

    /**
     * Handles document conversion failures during DOCX ↔ PDF conversion.
     */
    @ExceptionHandler(FileConversionException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileConversionException(FileConversionException e) {
        log.error("document conversion failed: ", e);
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Document conversion failed", e.getMessage());
    }

    /**
     * Handles failures during template file upload and processing.
     */
    @ExceptionHandler(TemplateUploadException.class)
    public ResponseEntity<ErrorResponseDTO> handleTemplateUploadException(TemplateUploadException e) {
        log.error("template upload failed: ", e);
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Template upload failed", e.getMessage());
    }

    /**
     * Handles failures during template download or decompression.
     */
    @ExceptionHandler(TemplateDownloadException.class)
    public ResponseEntity<ErrorResponseDTO> handleTemplateDownloadException(TemplateDownloadException e) {
        log.error("template download failed: ", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Template download failed", e.getMessage());
    }

    /**
     * Handles attempts to perform state transitions on a contract that is not
     * in the required state (e.g., terminating a non-ACTIVE contract).
     */
    @ExceptionHandler(InvalidContractStateException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidContractStateException(InvalidContractStateException e) {
        log.warn("invalid contract state transition: {}", e.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Invalid contract state", e.getMessage());
    }

    /**
     * Handles cases where a signed document is not yet available for download,
     * typically because the contract has not been signed yet.
     */
    @ExceptionHandler(SignedDocumentNotAvailableException.class)
    public ResponseEntity<ErrorResponseDTO> handleSignedDocumentNotAvailableException(SignedDocumentNotAvailableException e) {
        log.warn("signed document unavailable: {}", e.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Signed document not available", e.getMessage());
    }

    /**
     * Handles cases where a template field is submitted as part of a mapping
     * request but does not belong to the specified template.
     */
    @ExceptionHandler(TemplateFieldOwnershipException.class)
    public ResponseEntity<ErrorResponseDTO> handleTemplateFieldOwnershipException(TemplateFieldOwnershipException e) {
        log.warn("field ownership violation: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Field does not belong to template", e.getMessage());
    }

    /**
     * Handles missing mandatory field mappings during contract generation.
     */
    @ExceptionHandler(MissingMandatoryFieldException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingMandatoryFieldException(MissingMandatoryFieldException e) {
        log.warn("contract generation failed — missing mandatory fields: {}", e.getMissingFields());
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing required field mappings",
                "Missing mandatory fields: " + String.join(", ", e.getMissingFields()));
    }

    /**
     * Handles failures during contract document generation.
     */
    @ExceptionHandler(ContractGenerationFailException.class)
    public ResponseEntity<ErrorResponseDTO> handleContractGenerationFailException(ContractGenerationFailException e) {
        log.error("contract generation failed: ", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Contract generation failed", e.getMessage());
    }

    /**
     * Handles database validation constraint violations (CHECK constraints, triggers, etc.).
     */
    @ExceptionHandler(DatabaseValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseValidationException(DatabaseValidationException e) {
        log.warn("database validation failed: {} — {}", e.getConstraintName(), e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Data validation failed",
                e.getDetails() != null ? e.getDetails() : e.getMessage());
    }

    /**
     * Handles data access exceptions caused by constraint violations or type mismatches.
     * Translates known DB constraint patterns into user-friendly messages.
     */
    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidDataAccessException(InvalidDataAccessResourceUsageException e) {
        log.error("data access error: ", e);
        return buildResponse(HttpStatus.BAD_REQUEST, "Data validation error", resolveDataAccessMessage(e.getMessage()));
    }

    /**
     * Handles generic data access exceptions not covered by more specific handlers.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccessException(DataAccessException e) {
        log.error("database access error: ", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database error",
                "A database error occurred. Please contact support.");
    }

    /**
     * Catch-all for any unexpected exceptions to prevent 500 white-label error pages.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception e) {
        log.error("unexpected internal error: ", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "An unexpected error occurred. Please contact support.");
    }

    /**
     * Translates known DB constraint violation patterns into user-friendly messages.
     * Avoids exposing raw SQL or internal DB details to the client.
     */
    private String resolveDataAccessMessage(String message) {
        if (message == null) return "A data validation error occurred. Please check your input data.";
        if (message.contains("CHECK constraint"))        return "Data violates validation constraints.";
        if (message.contains("UNIQUE constraint"))       return "A record with this value already exists.";
        if (message.contains("FOREIGN KEY constraint"))  return "Referenced record does not exist.";
        if (message.contains("NOT NULL constraint"))     return "Required fields are missing.";
        if (message.contains("type") && message.contains("bytea")) return "Invalid file data format.";
        return "A data validation error occurred. Please check your input data.";
    }

    /**
     * Constructs a consistent ErrorResponseDTO with status, message, details, and timestamp.
     */
    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String message, String details) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponseDTO.builder()
                        .status(status.value())
                        .message(message)
                        .details(details)
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }
}