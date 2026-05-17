package clm.negotiation.controllers;

import clm.negotiation.dto.requests.CreateNegotiationRequest;
import clm.negotiation.dto.requests.UpdateNotesRequest;
import clm.negotiation.dto.responses.NegotiationResponseDTO;
import clm.negotiation.services.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@Tag(name = "Negotiations", description = "Manage contract renegotiation rounds")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/negotiations")
public class NegotiationController {

    private final NegotiationService service;

    @Operation(
        summary     = "Create a new negotiation",
        description = "Creates a new negotiation in SENT (pending review) status for an existing contract."
    )
    @ApiResponse(responseCode = "201", description = "Negotiation created",
        content = @Content(schema = @Schema(implementation = NegotiationResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    @PostMapping
    public ResponseEntity<NegotiationResponseDTO> create(@Valid @RequestBody CreateNegotiationRequest request) {
        NegotiationResponseDTO response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/negotiations/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get negotiation by ID")
    @ApiResponse(responseCode = "200", description = "Negotiation found",
        content = @Content(schema = @Schema(implementation = NegotiationResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Negotiation not found", content = @Content)
    @GetMapping("/{id}")
    public ResponseEntity<NegotiationResponseDTO> getById(
            @Parameter(description = "Negotiation ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "List negotiations for a contract")
    @ApiResponse(responseCode = "200", description = "Negotiations returned",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = NegotiationResponseDTO.class))))
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<NegotiationResponseDTO>> getByContractId(
            @Parameter(description = "Contract ID", required = true)
            @PathVariable Long contractId) {
        List<NegotiationResponseDTO> result = service.getByContractId(contractId);
        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @Operation(summary = "List negotiations for a client")
    @ApiResponse(responseCode = "200", description = "Negotiations returned",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = NegotiationResponseDTO.class))))
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<NegotiationResponseDTO>> getByClientId(
            @Parameter(description = "Client ID", required = true)
            @PathVariable Integer clientId) {
        List<NegotiationResponseDTO> result = service.getByClientId(clientId);
        return result.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }

    @Operation(
        summary     = "Accept negotiation",
        description = "Transitions a SENT negotiation to ACCEPTED and applies the new price/date to the contract."
    )
    @ApiResponse(responseCode = "200", description = "Negotiation accepted and contract updated",
        content = @Content(schema = @Schema(implementation = NegotiationResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Negotiation not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Negotiation is not in SENT status", content = @Content)
    @PatchMapping("/{id}/accept")
    public ResponseEntity<NegotiationResponseDTO> accept(
            @Parameter(description = "Negotiation ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(service.accept(id));
    }

    @Operation(
        summary     = "Reject negotiation",
        description = """
            Transitions a SENT negotiation to REJECTED.
            Optionally update the notes field to record the rejection reason.
            """
    )
    @ApiResponse(responseCode = "200", description = "Negotiation rejected",
        content = @Content(schema = @Schema(implementation = NegotiationResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Negotiation not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Negotiation is not in SENT status", content = @Content)
    @PatchMapping("/{id}/reject")
    public ResponseEntity<NegotiationResponseDTO> reject(
            @Parameter(description = "Negotiation ID", required = true)
            @PathVariable Long id,
            @RequestBody(required = false) UpdateNotesRequest request) {
        return ResponseEntity.ok(service.reject(id, request));
    }

    @Operation(
        summary     = "Update negotiation notes",
        description = "Updates the notes field at any stage of the negotiation."
    )
    @ApiResponse(responseCode = "200", description = "Notes updated",
        content = @Content(schema = @Schema(implementation = NegotiationResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Negotiation not found", content = @Content)
    @PatchMapping("/{id}/notes")
    public ResponseEntity<NegotiationResponseDTO> updateNotes(
            @Parameter(description = "Negotiation ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotesRequest request) {
        return ResponseEntity.ok(service.updateNotes(id, request));
    }
}
