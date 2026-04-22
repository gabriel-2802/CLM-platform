package clm.demo.controllers;

import clm.demo.dto.requests.GenAppendixRequest;
import clm.demo.dto.requests.UploadDirectAppendixRequest;
import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.exceptions.FileConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.AppendixService;
import clm.demo.services.download.DocumentDownloadService;
import clm.demo.utils.Utils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appendices")
public class AppendixController {

    private final AppendixService appendixService;
    private final DocumentDownloadService downloadService;

    /**
     * Generates a fillable appendix from a template and attaches it to a contract.
     * Returns {@code 201 Created}
     */
    @PostMapping("/generate")
    public ResponseEntity<AppendixResponseDTO> generateAppendix(@Valid @RequestBody GenAppendixRequest request) {
        AppendixResponseDTO response = appendixService.generateAppendix(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Uploads a non-fillable appendix directly (no template).
     * Returns {@code 201 Created}
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppendixResponseDTO> uploadDirectAppendix(
            @ModelAttribute @Valid UploadDirectAppendixRequest request) {

        if (request.getFile().isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        AppendixResponseDTO response = appendixService.uploadDirectAppendix(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Uploads a signed document for an existing DRAFT appendix and transitions it to SIGNED.
     */
    @PostMapping(value = "/{appendixId}/upload-signed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppendixResponseDTO> uploadSignedAppendix(
            @PathVariable Long appendixId,
            @RequestParam("file") @NotNull MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        try {
            return ResponseEntity.ok(appendixService.uploadSignedAppendix(appendixId, file.getBytes()));
        } catch (IOException e) {
            throw new FileConversionException("Failed to read uploaded file: " + e.getMessage(), e);
        }
    }

    /**
     * Lists all appendices for a contract.
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<AppendixResponseDTO>> getAppendicesForContract(@PathVariable Long contractId) {
        List<AppendixResponseDTO> result = appendixService.getAppendicesForContract(contractId);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result);
    }

    /**
     * Deletes an appendix and its field values.
     */
    @DeleteMapping("/{appendixId}")
    public ResponseEntity<Void> deleteAppendix(@PathVariable Long appendixId) {
        appendixService.deleteAppendix(appendixId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Downloads an appendix document.
     *
     * @param type   {@code signed} or {@code unsigned}
     * @param format {@code pdf} or {@code docx}
     */
    @GetMapping("/download/{appendixId}/{type}/{format}")
    public ResponseEntity<byte[]> downloadAppendix(
            @PathVariable @NotNull Long appendixId,
            @PathVariable @NotNull String type,
            @PathVariable @NotNull String format) {

        DocumentFormat documentFormat;
        try {
            documentFormat = DocumentFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid format: " + format + ". Supported: docx, pdf", e);
        }

        DocumentType documentType;
        try {
            documentType = DocumentType.valueOf(type.toUpperCase() + "_APPENDIX");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type: " + type + ". Supported: signed, unsigned", e);
        }

        byte[] content = downloadService.downloadDocument(appendixId, documentFormat, documentType);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=appendix-" + appendixId + "." + format.toLowerCase())
                .contentType(MediaType.parseMediaType(Utils.getContentType(documentFormat)))
                .body(content);
    }
}
