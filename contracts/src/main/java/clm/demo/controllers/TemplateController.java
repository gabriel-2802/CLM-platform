package clm.demo.controllers;

import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.responses.ParsedTemplateResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.dto.responses.BatchFieldMappingResponseDTO;
import clm.demo.services.TemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST controller for contract template lifecycle operations.
 * Handles HTTP request/response logic and delegates business logic to TemplateService.
 * Exception handling is centralized in GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Validated
public class TemplateController {

    private final TemplateService templateService;

    /**
     * Uploads a contract template file, parses it for placeholders,
     * and persists the template with its extracted fields.
     *
     * @param request containing the template file and metadata
     * @return 201 Created with parsed template details
     * @throws IOException if file parsing fails
     * @throws IllegalArgumentException if file is invalid
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParsedTemplateResponseDTO> uploadTemplate(@ModelAttribute @Valid UploadTemplateRequest request) throws IOException {
        ParsedTemplateResponseDTO response = templateService.uploadTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a template by ID with all its parsed fields and current mappings.
     *
     * @param templateId the template ID
     * @return 200 OK with template metadata, fields, and mappings
     * @throws RuntimeException if template not found
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponseDTO> getTemplate(@PathVariable @NotNull Long templateId) {
        TemplateResponseDTO response = templateService.getTemplate(templateId);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates multiple field mappings for a template in a single batch operation.
     * Maps multiple template fields to their corresponding database columns.
     * Called after user selects database columns for all template placeholders.
     *
     * @param request containing template ID and a list of field mapping definitions
     * @return 200 OK with batch mapping results and status for each field
     */
    @PutMapping("/mappings")
    public ResponseEntity<BatchFieldMappingResponseDTO> updateFieldMappings(@RequestBody @Valid FieldMappingRequest request) {
        BatchFieldMappingResponseDTO response = templateService.updateFieldMappings(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a template and cascades to all its fields, mappings, and generated contracts.
     * Prefer archiving via status update over deletion in production.
     *
     * @param templateId the template to delete
     * @return 204 No Content on success
     * @throws RuntimeException if template not found
     */
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable @NotNull Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }


}