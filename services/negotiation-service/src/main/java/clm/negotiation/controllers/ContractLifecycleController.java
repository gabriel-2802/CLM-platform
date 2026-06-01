package clm.negotiation.controllers;

import clm.negotiation.dto.requests.ContractActivatedRequest;
import clm.negotiation.dto.requests.ContractDeactivatedRequest;
import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.services.ContractLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Contract Lifecycle", description = "Internal hooks called by contract-service on contract lifecycle events")
@RestController
@RequestMapping("/api/negotiations/contracts")
@RequiredArgsConstructor
public class ContractLifecycleController {

    private final ContractLifecycleService lifecycleService;

    @Operation(
        summary     = "Contract activated (signed)",
        description = "Creates the default initial negotiation for the contract, dated at the contract start date."
    )
    @PostMapping("/activated")
    public ResponseEntity<NegotiationResponseDTO> contractActivated(
            @Valid @RequestBody ContractActivatedRequest request) {
        return ResponseEntity.ok(lifecycleService.onContractActivated(request));
    }

    @Operation(
        summary     = "Contracts deactivated (expired / terminated)",
        description = "Marks the given contracts as terminated so their clients are excluded from inactivity email notifications."
    )
    @PostMapping("/deactivated")
    public ResponseEntity<Void> contractsDeactivated(
            @Valid @RequestBody ContractDeactivatedRequest request) {
        lifecycleService.onContractsDeactivated(request);
        return ResponseEntity.ok().build();
    }
}
