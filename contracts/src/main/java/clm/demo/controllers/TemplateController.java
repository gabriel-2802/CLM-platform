package clm.demo.controllers;

import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.responses.ParsedTemplateResponseDTO;
import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.exceptions.FileConversionException;
import clm.demo.exceptions.UnsupportedConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.TemplateService;
import clm.demo.services.download.DocumentDownloadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
    private final DocumentDownloadService downloadService;

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
     * Retrieves all available templates with their metadata and field counts.
     * This endpoint must come before /{templateId} to properly route "/all" requests.
     *
     * @return 200 OK with list of all templates
     */
    @GetMapping("/all")
    public ResponseEntity<List<TemplateResponseDTO>> getAllTemplates() {
        var templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
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
    @PutMapping("/{templateId}/labels")
    public ResponseEntity<List<TemplateFieldResponseDTO>> updateFieldLabels(@RequestBody @Valid FieldMappingRequest request) {
       var response = templateService.updateFieldLabels(request);
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

    /**
     * Downloads a template in the specified format (DOCX or PDF).
     * If the template is stored in another format, automatically converts it.
     *
     * @param templateId the template ID to download
     * @param format the desired output format (docx or pdf)
     * @return 200 OK with the file as binary attachment
     * @throws IllegalArgumentException       if the format is invalid
     * @throws FileConversionException        if conversion fails
     * @throws UnsupportedConversionException if the format combination is unsupported
     * @throws IOException                    if decompression fails
     */
    @GetMapping("/download/{format}/{templateId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable @NotNull Long templateId, @PathVariable @NotNull String format) throws IOException {
        
        DocumentFormat documentFormat;
        try {
            documentFormat = DocumentFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid format: " + format + ". Supported formats: docx, pdf", e);
        }

        byte[] documentContent = downloadService.downloadDocument(templateId, documentFormat, DocumentType.TEMPLATE);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=template-" + templateId + "." + format.toLowerCase())
                .header("Content-Type", downloadService.getContentType(documentFormat))
                .body(documentContent);
    }

}

