package clm.demo.controllers;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.TemplateUploadResponseDTO;
import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.TemplateService;
import clm.demo.services.download.DocumentDownloadService;
import clm.demo.utils.Constants;
import clm.demo.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Handles HTTP request/response logic and delegates business logic to {@link TemplateService}.
 * Exception handling is centralised in {@link GlobalExceptionHandler}.
 */
@Tag(name = "Templates", description = "Upload and manage document templates with placeholder fields")
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final DocumentDownloadService downloadService;

    @Operation(
        summary     = "Upload a template",
        description = """
            Parses a DOCX or PDF file for dot-sequence placeholders (4+ dots), normalises them,
            and persists the template with its extracted fields.
            PDFs are automatically converted to DOCX internally before storage.
            Returns the parsed document text with placeholders replaced by `{{fieldId}}` tokens.
            """
    )
    @ApiResponse(responseCode = "201", description = "Template created successfully",
        content = @Content(schema = @Schema(implementation = TemplateUploadResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Empty file or duplicate template name",
        content = @Content)
    @ApiResponse(responseCode = "415", description = "File is not DOCX or PDF",
        content = @Content)
    @ApiResponse(responseCode = "422", description = "Document parse or conversion failure",
        content = @Content)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TemplateUploadResponseDTO> uploadTemplate(
            @ModelAttribute @Valid UploadTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.uploadTemplate(request));
    }


    @Operation(
        summary     = "List all templates",
        description = "Returns a paginated list of all templates ordered by creation date descending."
    )
    @ApiResponse(responseCode = "200", description = "Templates returned")
    @ApiResponse(responseCode = "204", description = "No templates found", content = @Content)
    @GetMapping
    public ResponseEntity<List<TemplateResponseDTO>> getAllTemplates(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE) int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {

        Page<TemplateResponseDTO> result = templateService.getAllTemplates(page, size);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result.getContent());
    }

    @Operation(
        summary     = "Get a template by ID",
        description = "Retrieves a single template with all its parsed fields and current label mappings."
    )
    @ApiResponse(responseCode = "200", description = "Template found",
        content = @Content(schema = @Schema(implementation = TemplateResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Template not found", content = @Content)
    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponseDTO> getTemplate(
            @Parameter(description = "Template ID", required = true, example = "7")
            @PathVariable @NotNull Long templateId) {
        return ResponseEntity.ok(templateService.getTemplate(templateId));
    }

    @Operation(
        summary     = "Map field labels on a template",
        description = """
            Batch-updates label, data type, required flag, and format pattern for one or more
            template fields. Sets `isFullyMapped = true` on the template when every required
            field has been assigned a label.
            The `templateId` in the request body must match the path parameter.
            """
    )
    @ApiResponse(responseCode = "200", description = "Fields updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error or field belongs to a different template",
        content = @Content)
    @ApiResponse(responseCode = "404", description = "Template or field not found",
        content = @Content)
    @PutMapping("/{templateId}/labels")
    public ResponseEntity<List<TemplateFieldResponseDTO>> updateFieldLabels(
            @Parameter(description = "Template ID the fields belong to", required = true, example = "7")
            @PathVariable @NotNull Long templateId,
            @RequestBody @Valid FieldMappingRequest request) {
        return ResponseEntity.ok(templateService.updateFieldLabels(request));
    }


    @Operation(
        summary     = "Delete a template",
        description = """
            Deletes the template and cascades to all its `TemplateField` rows.
            Blocked (409) if any contract or appendix still references this template.
            """
    )
    @ApiResponse(responseCode = "204", description = "Template deleted", content = @Content)
    @ApiResponse(responseCode = "404", description = "Template not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Template is referenced by existing documents",
        content = @Content)
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "Template ID", required = true, example = "7")
            @PathVariable @NotNull Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }


    @Operation(
        summary     = "Download a template file",
        description = "Downloads the template binary in the requested format. Converts on the fly if the stored format differs."
    )
    @ApiResponse(responseCode = "200", description = "File returned as a binary attachment")
    @ApiResponse(responseCode = "400", description = "Invalid format value", content = @Content)
    @ApiResponse(responseCode = "404", description = "Template not found", content = @Content)
    @ApiResponse(responseCode = "422", description = "Format conversion failure", content = @Content)
    @GetMapping("/download/{templateId}/{format}")
    public ResponseEntity<byte[]> downloadTemplate(
            @Parameter(description = "Template ID", required = true, example = "7")
            @PathVariable @NotNull Long templateId,
            @Parameter(description = "Output format", required = true,
                       schema = @Schema(allowableValues = {"docx", "pdf"}))
            @PathVariable @NotNull String format) {

        DocumentFormat documentFormat;
        try {
            documentFormat = DocumentFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid format: " + format + ". Supported: docx, pdf", e);
        }

        byte[] content = downloadService.downloadDocument(templateId, documentFormat, DocumentType.TEMPLATE);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=template-" + templateId + "." + format.toLowerCase())
                .contentType(MediaType.parseMediaType(Utils.getContentType(documentFormat)))
                .body(content);
    }
}
