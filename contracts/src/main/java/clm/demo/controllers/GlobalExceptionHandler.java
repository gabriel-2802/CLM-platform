package clm.demo.controllers;

import clm.demo.dto.responses.ErrorResponseDTO;
import clm.demo.exceptions.DatabaseValidationException;
import clm.demo.exceptions.EmptyFileNameException;
import clm.demo.exceptions.FileConversionException;
import clm.demo.exceptions.MissingMandatoryFieldException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.UnsupportedConversionException;
import clm.demo.exceptions.UnsupportedFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Intercepts exceptions across all controllers to provide consistent JSON error responses.
 * This prevents the leaking of internal stack traces to the client.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors in requests (e.g., empty files, invalid data).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid argument provided: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request", e.getMessage());
    }

    /**
     * Handles binary/file-system level failures during compression or parsing.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponseDTO> handleIOException(IOException e) {
        log.error("Document binary processing failed: ", e);
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "Document processing failed", e.getMessage());
    }

    /**
     * Handles cases where a file is uploaded without a name or is completely empty.
     */
    @ExceptionHandler(EmptyFileNameException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmptyFileNameException(EmptyFileNameException e) {
        log.warn("Upload rejected: Empty file name provided.");
        return buildResponse(HttpStatus.BAD_REQUEST, "Template upload failed", e.getMessage());
    }

    /**
     * Handles cases where the user uploads a format other than PDF or DOCX.
     */
    @ExceptionHandler(UnsupportedFileException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnsupportedFileException(UnsupportedFileException e) {
        log.warn("Upload rejected: Unsupported file type. {}", e.getMessage());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported file format", e.getMessage());
    }

    /**
     * Handles unsupported format conversion requests (e.g., PDF to PDF).
     */
    @ExceptionHandler(UnsupportedConversionException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnsupportedConversionException(UnsupportedConversionException e) {
        log.warn("Format conversion not supported: {}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Unsupported format conversion", e.getMessage());
    }

    /**
     * Handles document conversion failures during DOCX ↔ PDF conversion.
     */
    @ExceptionHandler(FileConversionException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileConversionException(FileConversionException e) {
        log.error("Document conversion failed: ", e);
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "Document conversion failed", e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", e.getMessage());
    }

    /**
     * Handles missing mandatory field mappings during contract generation.
     * Provides detailed information about which fields are missing values.
     */
    @ExceptionHandler(MissingMandatoryFieldException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingMandatoryFieldException(MissingMandatoryFieldException e) {
        log.warn("Contract generation failed due to missing mandatory fields: {}", e.getMissingFields());
        String details = "Missing mandatory fields: " + String.join(", ", e.getMissingFields());
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing required field mappings", details);
    }

    /**
     * Handles database validation constraint violations (CHECK constraints, triggers, etc.).
     */
    @ExceptionHandler(DatabaseValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabaseValidationException(DatabaseValidationException e) {
        log.warn("Database validation failed: {} - {}", e.getConstraintName(), e.getMessage());
        String details = e.getDetails() != null ? e.getDetails() : e.getMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, "Data validation failed", details);
    }

    /**
     * Handles data access exceptions from the database layer (constraint violations, type mismatches, etc.).
     */
    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidDataAccessException(InvalidDataAccessResourceUsageException e) {
        log.error("Data access error occurred: ", e);
        
        String message = e.getMessage();
        String details = "A data validation error occurred. Please check your input data.";

        if (message != null) {
            if (message.contains("CHECK constraint")) {
                details = "Data violates validation constraints.";
            } else if (message.contains("UNIQUE constraint")) {
                details = "A record with this value already exists.";
            } else if (message.contains("FOREIGN KEY constraint")) {
                details = "Referenced record does not exist.";
            } else if (message.contains("NOT NULL constraint")) {
                details = "Required fields are missing.";
            } else if (message.contains("type") && message.contains("bytea")) {
                details = "Invalid file data format. Please ensure the file is properly formatted.";
            }
        }
        
        return buildResponse(HttpStatus.BAD_REQUEST, "Data validation error", details);
    }

    /**
     * Handles generic data access exceptions.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccessException(DataAccessException e) {
        log.error("Database access error: ", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", "A database error occurred. Please contact support.");
    }

    /**
     * Catch-all for any unexpected runtime exceptions to avoid 500 white-label pages.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneralException(Exception e) {
        log.error("An unexpected internal error occurred: ", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred. Please contact support.");
    }

    /**
     *  construct the ErrorResponseDTO.
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