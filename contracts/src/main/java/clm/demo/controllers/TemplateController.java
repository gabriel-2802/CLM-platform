package clm.demo.controllers;

import clm.demo.dto.requests.FieldMappingRequest;
import clm.demo.dto.requests.UploadTemplateRequest;
import clm.demo.dto.responses.TemplateFieldResponseDTO;
import clm.demo.dto.responses.TemplateResponseDTO;
import clm.demo.dto.responses.TemplateUploadResponseDTO;
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

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final DocumentDownloadService downloadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TemplateUploadResponseDTO> uploadTemplate(@ModelAttribute @Valid UploadTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.uploadTemplate(request));
    }

    @GetMapping
    public ResponseEntity<List<TemplateResponseDTO>> getAllTemplates(
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size) {
        Page<TemplateResponseDTO> result = templateService.getAllTemplates(page, size);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result.getContent());
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponseDTO> getTemplate(@PathVariable @NotNull Long templateId) {
        return ResponseEntity.ok(templateService.getTemplate(templateId));
    }

    @PutMapping("/{templateId}/labels")
    public ResponseEntity<List<TemplateFieldResponseDTO>> updateFieldLabels(@RequestBody @Valid FieldMappingRequest request) {
        return ResponseEntity.ok(templateService.updateFieldLabels(request));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable @NotNull Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{templateId}/{format}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable @NotNull Long templateId,
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
