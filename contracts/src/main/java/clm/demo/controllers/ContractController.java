package clm.demo.controllers;

import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.exceptions.FileConversionException;
import clm.demo.exceptions.UnsupportedConversionException;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.models.enums.DocumentType;
import clm.demo.services.ContractService;
import clm.demo.services.download.DocumentDownloadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/contracts")
public class ContractController {
    private final ContractService contractService;
    private final DocumentDownloadService downloadService;

    @PostMapping("/generate")
    ResponseEntity<ContractResponseDTO> generateContracts(@Valid @RequestBody GenContractRequest request) {
        ContractResponseDTO responseDTO = contractService.generateContract(request);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping(value = "upload/signed/{contractId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> uploadSignedContract(@PathVariable Long contractId, @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        contractService.uploadSignedContract(contractId, file.getBytes());
        return ResponseEntity.ok("Signed contract uploaded successfully and status updated to ACTIVE");
    }

    @PutMapping("terminate/{contractId}")
    ResponseEntity<String> terminateContract(@PathVariable Long contractId, @RequestBody @Valid ContractTerminationRequest request) {
        return null;
    }

    @GetMapping("/all")
    ResponseEntity<List<ContractResponseDTO>> getAllContracts() {
        return contractService.getAll();
    }

    /**
     * Downloads a contract in the specified format (DOCX or PDF).
     * If the contract is stored in another format, automatically converts it.
     * Supports both SIGNED and UNSIGNED contracts.
     *
     * @param contractId the contract ID to download
     * @param type the contract type (signed or unsigned)
     * @param format the desired output format (docx or pdf)
     * @return 200 OK with the file as binary attachment
     * @throws IllegalArgumentException       if the type or format is invalid
     * @throws FileConversionException        if conversion fails
     * @throws UnsupportedConversionException if the format combination is unsupported
     * @throws IOException                    if decompression fails
     */
    @GetMapping("/download/{type}/{format}/{contractId}")
    ResponseEntity<byte[]> downloadContract(
            @PathVariable @NotNull Long contractId,
            @PathVariable @NotNull String type,
            @PathVariable @NotNull String format) throws IOException {
        
        DocumentFormat documentFormat;
        try {
            documentFormat = DocumentFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid format: " + format + ". Supported formats: docx, pdf", e);
        }

        DocumentType documentType;
        try {
            documentType = DocumentType.valueOf(type.toUpperCase() + "_CONTRACT");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid contract type: " + type + ". Supported types: signed, unsigned", e);
        }

        byte[] documentContent = downloadService.downloadDocument(contractId, documentFormat, documentType);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=contract-" + contractId + "." + format.toLowerCase())
                .header("Content-Type", downloadService.getContentType(documentFormat))
                .body(documentContent);
    }
    
    
}

