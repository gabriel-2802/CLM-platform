package clm.demo.controllers;

import clm.demo.dto.responses.ErrorResponseDTO;
import clm.demo.exceptions.EmptyFileNameException;
import clm.demo.exceptions.ResourceNotFoundException;
import clm.demo.exceptions.UnsupportedFileException;
import lombok.extern.slf4j.Slf4j;
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", e.getMessage());
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