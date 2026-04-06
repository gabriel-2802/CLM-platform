package clm.demo.controllers;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.TemplateUploadResponseDTO;
import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.dto.responses.AvailableFieldResponseDTO;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.TemplateService;
import clm.demo.services.download.DocumentDownloadService;
import clm.demo.utils.Constants;
import clm.demo.utils.Utils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for contract template lifecycle operations.
 * Handles HTTP request/response logic and delegates business logic to
 * TemplateService.
 * Exception handling is centralized in GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final DocumentDownloadService downloadService;

    /**
     * Uploads a contract template file, parses it for placeholders,
     * and persists the template with its extracted fields.
     *
     * @param request containing the template file and metadata
     * @return 201 Created with parsed template details
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TemplateUploadResponseDTO> uploadTemplate(
            @ModelAttribute @Valid UploadTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.uploadTemplate(request));
    }

    /**
     * Retrieves all templates with pagination, sorted by creation date descending.
     *
     * @param page zero-based page index (default 0)
     * @param size number of records per page (default 20)
     * @return 200 OK with page of templates, or 204 No Content if none found
     */
    @GetMapping
    public ResponseEntity<List<TemplateResponseDTO>> getAllTemplates(
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        Page<TemplateResponseDTO> result = templateService.getAllTemplates(page, size);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result.getContent());
    }

    /**
     * Retrieves a template by ID with all its parsed fields and current mappings.
     *
     * @param templateId the template ID
     * @return 200 OK with template metadata and fields
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponseDTO> getTemplate(@PathVariable @NotNull Long templateId) {
        return ResponseEntity.ok(templateService.getTemplate(templateId));
    }

    /**
     * Batch updates field mappings for a template.
     * Maps multiple template placeholders to labels, data types, and validation
     * rules.
     *
     * @param request containing template ID and list of field mapping definitions
     * @return 200 OK with updated field details
     */
    @PutMapping("/{templateId}/labels")
    public ResponseEntity<List<TemplateFieldResponseDTO>> updateFieldLabels(
            @RequestBody @Valid FieldMappingRequest request) {
        return ResponseEntity.ok(templateService.updateFieldLabels(request));
    }

    /**
     * Deletes a template and cascades to all its fields and mappings.
     *
     * @param templateId the template to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable @NotNull Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Downloads a template in the specified format (DOCX or PDF).
     * Converts automatically if the stored format differs from the requested one.
     *
     * @param templateId the template ID to download
     * @param format     the desired output format (docx or pdf)
     * @return 200 OK with the file as a binary attachment
     */
    @GetMapping("/download/{templateId}/{format}")
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable @NotNull Long templateId,
            @PathVariable @NotNull String format) {

        DocumentFormat documentFormat;
        try {
            documentFormat = DocumentFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid format: " + format + ". Supported: docx, pdf", e);
        }

        byte[] content = downloadService.downloadDocument(templateId, documentFormat, DocumentType.TEMPLATE);

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=template-" + templateId + "." + format.toLowerCase())
                .contentType(MediaType.parseMediaType(Utils.getContentType(documentFormat)))
                .body(content);
    }

    /**
     * Retrieves the list of fields available for mapping placeholders to database data.
     * Includes client properties (name, cui, etc.) and contract metadata.
     *
     * @return 200 OK with grouped available fields
     */
    @GetMapping("/available-mapping-fields")
    public ResponseEntity<AvailableFieldResponseDTO> getAvailableMappingFields() {
        return ResponseEntity.ok(templateService.getAvailableMappingFields());
    }
}