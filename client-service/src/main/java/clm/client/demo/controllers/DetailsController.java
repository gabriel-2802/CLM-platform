package clm.client.demo.controllers;

import clm.client.demo.dtos.request.DetailsRequest;
import clm.client.demo.dtos.response.DetailsResponse;
import clm.client.demo.services.DetailsService;
import clm.client.demo.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients/{clientId}/detalii")
@RequiredArgsConstructor
@Tag(name = "Details", description = "Client details (Detalii) management - additional compliance and audit information")
@SecurityRequirement(name = "bearerAuth")
public class DetailsController {

    private final DetailsService detailsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get client details",
            description = "Retrieves detailed compliance and audit information for a specific client. One details record per client."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Details retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - USER not assigned to this client or insufficient role"),
            @ApiResponse(responseCode = "404", description = "Client or details not found")
    })
    public ResponseEntity<DetailsResponse> getDetails(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(detailsService.getDetails(clientId));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Create or replace client details",
            description = "Creates or completely replaces the details record for a client. If details already exist, they are replaced. Requires MANAGER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Details created or replaced successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires MANAGER or ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<DetailsResponse> createOrReplaceDetails(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details request (full replacement)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DetailsRequest.class))
            )
            @Validated(ValidationGroups.Create.class) @RequestBody DetailsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(detailsService.upsertDetails(clientId, request));
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Partially update client details",
            description = "Performs a partial update of client details. Only provided fields are updated. Requires MANAGER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Details partially updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires MANAGER or ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client or details not found")
    })
    public ResponseEntity<DetailsResponse> partialUpdateDetails(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details partial update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DetailsRequest.class))
            )
            @Valid @RequestBody DetailsRequest request) {
        return ResponseEntity.ok(detailsService.patchDetails(clientId, request));
    }
}