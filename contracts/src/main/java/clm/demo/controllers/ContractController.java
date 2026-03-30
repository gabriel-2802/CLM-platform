package clm.demo.controllers;


import clm.demo.dto.requests.ContractTerminationRequest;
import clm.demo.dto.requests.GenContractRequest;
import clm.demo.dto.responses.ContractResponseDTO;
import clm.demo.models.enums.DocumentFormat;
import clm.demo.services.ContractService;
import clm.demo.services.download.DocumentDownloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<String> uploadSignedContract(@PathVariable Long contractId, @RequestParam("file") byte[] file) {
        return null;
    }

    @PutMapping("terminate/{contractId}")
    ResponseEntity<String> terminateContract(@PathVariable Long contractId, @RequestBody @Valid ContractTerminationRequest request) {
        return null;
    }

    @GetMapping("/all")
    ResponseEntity<List<ContractResponseDTO>> getAllContracts() {
        return contractService.getAll();
    }

    @GetMapping("download/pdf/{contractId}")
    ResponseEntity<byte[]> downloadContractPdf(@PathVariable Long contractId) throws IOException {
        byte[] documentContent = downloadService.downloadContract(contractId);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=contract-" + contractId + ".pdf")
                .header("Content-Type", downloadService.getContentType(DocumentFormat.PDF))
                .body(documentContent);
    }
}

