package clm.demo.controllers;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.exceptions.FileConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.ContractService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@Tag(name = "Contracts", description = "Generate, sign, terminate, search, and download contracts")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;
    private final DocumentDownloadService downloadService;

    @Operation(
        summary     = "Generate a contract from a template",
        description = """
            Fills the specified fully-mapped template with the provided field values, converts
            the result to PDF, and persists the contract in `PENDING_SIGNATURE` state.
            Returns `201 Created` with a `Location` header pointing to the new resource.
            """
    )
    @ApiResponse(responseCode = "201", description = "Contract created — Location header points to the new resource",
        headers = @Header(name = "Location", description = "URI of the created contract"),
        content = @Content(schema = @Schema(implementation = ContractResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Missing required field value in mappings",
        content = @Content)
    @ApiResponse(responseCode = "404", description = "Template not found",
        content = @Content)
    @ApiResponse(responseCode = "422", description = "Contract generation or DOCX rendering failed",
        content = @Content)
    @PostMapping("/generate")
    public ResponseEntity<ContractResponseDTO> generateContract(
            @Valid @RequestBody GenContractRequest request) {

        ContractResponseDTO response = contractService.generateContract(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/contracts/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
        summary     = "Upload signed document",
        description = """
            Attaches a signed document to a `PENDING_SIGNATURE` contract, converts DOCX uploads
            to PDF internally, and transitions the contract status to `ACTIVE`.
            """
    )
    @ApiResponse(responseCode = "200", description = "Contract is now ACTIVE",
        content = @Content(schema = @Schema(implementation = ContractResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Empty file", content = @Content)
    @ApiResponse(responseCode = "404", description = "Contract not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Contract is not in PENDING_SIGNATURE state",
        content = @Content)
    @ApiResponse(responseCode = "422", description = "PDF conversion failed", content = @Content)
    @PostMapping(value = "/{contractId}/upload-signed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractResponseDTO> uploadSignedContract(
            @Parameter(description = "Contract ID", required = true, example = "88")
            @PathVariable Long contractId,
            @Parameter(description = "Signed DOCX or PDF file", required = true)
            @RequestParam("file") @NotNull MultipartFile file) {

        if (file.isEmpty()) throw new IllegalArgumentException("File cannot be empty");

        try {
            return ResponseEntity.ok(contractService.uploadSignedContract(contractId, file.getBytes()));
        } catch (IOException e) {
            throw new FileConversionException("Failed to read uploaded file: " + e.getMessage(), e);
        }
    }

    @Operation(
        summary     = "Terminate an ACTIVE contract",
        description = "Records the termination date and reason, and transitions the contract status to `TERMINATED`. Only `ACTIVE` contracts can be terminated."
    )
    @ApiResponse(responseCode = "204", description = "Contract terminated", content = @Content)
    @ApiResponse(responseCode = "404", description = "Contract not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Contract is not ACTIVE", content = @Content)
    @PutMapping("/terminate/{contractId}")
    public ResponseEntity<Void> terminateContract(
            @Parameter(description = "Contract ID", required = true, example = "88")
            @PathVariable Long contractId,
            @Valid @RequestBody ContractTerminationRequest request) {
        contractService.terminateContract(contractId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary     = "Toggle auto-renewal status",
        description = """
            Toggles the auto-renewal flag for a contract. If `autoRenew` is `false`, it will be set to `true`
            and vice versa. This can be applied to contracts in any status.
            """
    )
    @ApiResponse(responseCode = "200", description = "Auto-renewal status toggled",
        content = @Content(schema = @Schema(implementation = ContractResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Contract not found", content = @Content)
    @PutMapping("/{contractId}/toggle-auto-renew")
    public ResponseEntity<ContractResponseDTO> toggleAutoRenewal(
            @Parameter(description = "Contract ID", required = true, example = "88")
            @PathVariable Long contractId) {
        log.info("Toggling auto-renewal status for contract {}", contractId);
        ContractResponseDTO response = contractService.toggleAutoRenewal(contractId);
        return ResponseEntity.ok(response);
    }


    @Operation(
        summary     = "List all contracts",
        description = "Returns a paginated list of all contracts ordered by creation date descending."
    )
    @ApiResponse(responseCode = "200", description = "Contracts returned",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractResponseDTO.class))))
    @ApiResponse(responseCode = "204", description = "No contracts found", content = @Content)
    @GetMapping("/all")
    public ResponseEntity<List<ContractResponseDTO>> getAll(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Page<ContractResponseDTO> result = contractService.getAll(page, size);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result.getContent());
    }

    @Operation(
        summary     = "Search contracts",
        description = """
            Filters contracts by optional criteria: status, client, creator, date range, notes, and
            injected field values (trigram substring match). Uses `POST` because several HTTP clients
            and intermediaries do not support GET requests with a body.
            Returns `204 No Content` when no contracts match.
            """
    )
    @ApiResponse(responseCode = "200", description = "Matching contracts returned",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractResponseDTO.class))))
    @ApiResponse(responseCode = "204", description = "No contracts match the given filters",
        content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid filter values", content = @Content)
    @PostMapping("/search")
    public ResponseEntity<List<ContractResponseDTO>> search(
            @RequestBody SearchRequest request) {
        Page<ContractResponseDTO> result = contractService.search(request);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result.getContent());
    }

    @Operation(
        summary     = "Download a contract file",
        description = "Downloads the unsigned or signed contract binary in DOCX or PDF format."
    )
    @ApiResponse(responseCode = "200", description = "File returned as binary attachment")
    @ApiResponse(responseCode = "400", description = "Invalid type or format value", content = @Content)
    @ApiResponse(responseCode = "404", description = "Contract not found, or signed document not yet uploaded",
        content = @Content)
    @ApiResponse(responseCode = "422", description = "Format conversion failed", content = @Content)
    @GetMapping("/download/{contractId}/{type}/{format}")
    public ResponseEntity<byte[]> downloadContract(
            @Parameter(description = "Contract ID", required = true, example = "88")
            @PathVariable @NotNull Long contractId,
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
            documentType = DocumentType.valueOf(type.toUpperCase() + "_CONTRACT");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type: " + type + ". Supported: signed, unsigned", e);
        }

        log.info("Downloading contract {} in format {} (type: {})", contractId, format, type);

        byte[] content = downloadService.downloadDocument(contractId, documentFormat, documentType);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=contract-" + contractId + "." + format.toLowerCase())
                .contentType(MediaType.parseMediaType(Utils.getContentType(documentFormat)))
                .body(content);
    }


}
