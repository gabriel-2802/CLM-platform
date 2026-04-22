package clm.demo.controllers;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.requests.SearchRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.FileConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.ContractService;
import clm.demo.services.download.DocumentDownloadService;
import clm.demo.utils.Utils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;
    private final DocumentDownloadService downloadService;

    /**
     * Generates a new contract from a template.
     * Returns {@code 201 Created} with a {@code Location} header pointing to the new resource.
     */
    @PostMapping("/generate")
    public ResponseEntity<ContractResponseDTO> generateContract(@Valid @RequestBody GenContractRequest request) {
        ContractResponseDTO response = contractService.generateContract(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/contracts/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Uploads a signed document for an existing {@code PENDING_SIGNATURE} contract
     * and transitions it to {@code ACTIVE}.
     */
    @PostMapping(value = "/{contractId}/upload-signed", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContractResponseDTO> uploadSignedContract(
            @PathVariable Long contractId,
            @RequestParam("file") @NotNull MultipartFile file) {

        if (file.isEmpty()) throw new IllegalArgumentException("File cannot be empty");

        try {
            return ResponseEntity.ok(contractService.uploadSignedContract(contractId, file.getBytes()));
        } catch (IOException e) {
            throw new FileConversionException("Failed to read uploaded file: " + e.getMessage(), e);
        }
    }

    /**
     * Terminates an {@code ACTIVE} contract.
     */
    @PutMapping("/terminate/{contractId}")
    public ResponseEntity<Void> terminateContract(@PathVariable Long contractId,
                                                   @Valid @RequestBody ContractTerminationRequest request) {
        contractService.terminateContract(contractId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ContractResponseDTO>> getAll(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        Page<ContractResponseDTO> result = contractService.getAll(page, size);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result.getContent());
    }

    /**
     * Searches contracts with optional filters.
     *
     * <p>Uses {@code POST} rather than {@code GET} because GET requests with a body are
     * not supported by several HTTP clients and intermediaries (CDNs, proxies).</p>
     */
    @PostMapping("/search")
    public ResponseEntity<List<ContractResponseDTO>> search(@RequestBody SearchRequest request) {
        Page<ContractResponseDTO> result = contractService.search(request);
        return result.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(result.getContent());
    }

    /**
     * Downloads a contract in the specified format (DOCX or PDF).
     *
     * @param type   {@code signed} or {@code unsigned}
     * @param format {@code pdf} or {@code docx}
     */
    @GetMapping("/download/{contractId}/{type}/{format}")
    public ResponseEntity<byte[]> downloadContract(@PathVariable @NotNull Long contractId,
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
