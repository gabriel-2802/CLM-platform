package clm.demo.controllers;

import clm.demo.dto.requests.GenAppendixRequest;
import clm.demo.dto.requests.UploadDirectAppendixRequest;
import clm.demo.dto.responses.AppendixResponseDTO;
import clm.demo.exceptions.exceptions.FileConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.AppendixService;
import clm.demo.services.download.DocumentDownloadService;
import clm.demo.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Appendices", description = "Generate, upload, sign, and download contract appendices")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/appendices")
public class AppendixController {

    private final AppendixService appendixService;
    private final DocumentDownloadService downloadService;

    @Operation(
        summary     = "Generate an appendix from a template",
        description = """
            Fills the specified template with the provided field values, generates a PDF,
            and attaches it to the parent contract as a `DRAFT` appendix.
            Returns `201 Created` with a `Location` header pointing to the new resource.
            """
    )
    @ApiResponse(responseCode = "201", description = "Appendix created in DRAFT state",
        headers = @Header(name = "Location", description = "URI of the created appendix"),
        content = @Content(schema = @Schema(implementation = AppendixResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Missing required field value in mappings",
        content = @Content)
    @ApiResponse(responseCode = "404", description = "Contract or template not found",
        content = @Content)
    @ApiResponse(responseCode = "422", description = "Document generation failed",
        content = @Content)
    @PostMapping("/generate")
    public ResponseEntity<AppendixResponseDTO> generateAppendix(
            @Valid @RequestBody GenAppendixRequest request) {

        AppendixResponseDTO response = appendixService.generateAppendix(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
        summary     = "Upload a non-fillable appendix directly",
        description = """
            Uploads a DOCX or PDF directly as an appendix (no template required).
            The file format is auto-detected from magic bytes.
            Direct uploads are immediately set to `SIGNED` — no separate sign step needed.
            Returns `201 Created` with a `Location` header pointing to the new resource.
            """
    )
    @ApiResponse(responseCode = "201", description = "Appendix created and immediately SIGNED",
        headers = @Header(name = "Location", description = "URI of the created appendix"),
        content = @Content(schema = @Schema(implementation = AppendixResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Empty file or missing required fields",
        content = @Content)
    @ApiResponse(responseCode = "404", description = "Contract not found",
        content = @Content)
    @ApiResponse(responseCode = "415", description = "File is not DOCX or PDF",
        content = @Content)
    @ApiResponse(responseCode = "422", description = "Storage failure",
        content = @Content)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppendixResponseDTO> uploadDirectAppendix(
            @ModelAttribute @Valid UploadDirectAppendixRequest request) {

        if (request.getFile().isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        AppendixResponseDTO response = appendixService.uploadDirectAppendix(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
        summary     = "Upload signed document for a DRAFT appendix",
        description = """
            Attaches a signed document to a `DRAFT` appendix, converts DOCX to PDF internally,
            and transitions the appendix status to `SIGNED`.
            Already-`SIGNED` appendices are rejected with `409 Conflict`.
            """
    )
    @ApiResponse(responseCode = "200", description = "Appendix is now SIGNED",
        content = @Content(schema = @Schema(implementation = AppendixResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Empty file", content = @Content)
    @ApiResponse(responseCode = "404", description = "Appendix not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Appendix is already SIGNED", content = @Content)
    @ApiResponse(responseCode = "422", description = "PDF conversion failed", content = @Content)
    @PostMapping(value = "/{appendixId}/upload-signed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppendixResponseDTO> uploadSignedAppendix(
            @Parameter(description = "Appendix ID", required = true, example = "12")
            @PathVariable Long appendixId,
            @Parameter(description = "Signed DOCX or PDF file", required = true)
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


    @Operation(
        summary     = "List all appendices for a contract",
        description = "Returns all appendices attached to the specified contract, ordered by creation date."
    )
    @ApiResponse(responseCode = "200", description = "Appendices returned",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppendixResponseDTO.class))))
    @ApiResponse(responseCode = "204", description = "No appendices found for this contract",
        content = @Content)
    @ApiResponse(responseCode = "404", description = "Contract not found", content = @Content)
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<AppendixResponseDTO>> getAppendicesForContract(
            @Parameter(description = "Parent contract ID", required = true, example = "88")
            @PathVariable Long contractId) {

        List<AppendixResponseDTO> result = appendixService.getAppendicesForContract(contractId);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result);
    }


    @Operation(
        summary     = "Delete an appendix",
        description = "Deletes the appendix and cascades to its audit-trail field values."
    )
    @ApiResponse(responseCode = "204", description = "Appendix deleted", content = @Content)
    @ApiResponse(responseCode = "404", description = "Appendix not found", content = @Content)
    @DeleteMapping("/{appendixId}")
    public ResponseEntity<Void> deleteAppendix(
            @Parameter(description = "Appendix ID", required = true, example = "12")
            @PathVariable Long appendixId) {
        appendixService.deleteAppendix(appendixId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary     = "Download an appendix file",
        description = "Downloads the unsigned or signed appendix binary in DOCX or PDF format."
    )
    @ApiResponse(responseCode = "200", description = "File returned as binary attachment")
    @ApiResponse(responseCode = "400", description = "Invalid type or format value", content = @Content)
    @ApiResponse(responseCode = "404", description = "Appendix not found, or signed document not yet uploaded",
        content = @Content)
    @ApiResponse(responseCode = "422", description = "Format conversion failed", content = @Content)
    @GetMapping("/download/{appendixId}/{type}/{format}")
    public ResponseEntity<byte[]> downloadAppendix(
            @Parameter(description = "Appendix ID", required = true, example = "12")
            @PathVariable @NotNull Long appendixId,
            @Parameter(description = "Document version", required = true,
                       schema = @Schema(allowableValues = {"unsigned", "signed"}))
            @PathVariable @NotNull String type,
            @Parameter(description = "Output format", required = true,
                       schema = @Schema(allowableValues = {"docx", "pdf"}))
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
