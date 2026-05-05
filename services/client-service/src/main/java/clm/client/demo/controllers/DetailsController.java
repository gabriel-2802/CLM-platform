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
@RequestMapping("/api/clients/{clientId}/details")
@RequiredArgsConstructor
@Tag(name = "Details", description = "Client details management — compliance and audit information.")
@SecurityRequirement(name = "bearerAuth")
public class DetailsController {

    private final DetailsService detailsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get client details",
            description = "Retrieves compliance and audit information for a specific client.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "details retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetailsResponse.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client or details not found", content = @Content)
            }
    )
    public ResponseEntity<DetailsResponse> getDetails(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(detailsService.getDetails(clientId));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Create or replace client details",
            description = "Creates or fully replaces the details record for a client.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "details created or replaced",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetailsResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<DetailsResponse> createOrReplaceDetails(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Validated(ValidationGroups.Create.class) @RequestBody DetailsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(detailsService.upsertDetails(clientId, request));
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Partially update client details",
            description = "Updates only the provided fields of the client details record.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "details partially updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetailsResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client or details not found", content = @Content)
            }
    )
    public ResponseEntity<DetailsResponse> partialUpdateDetails(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Valid @RequestBody DetailsRequest request) {
        return ResponseEntity.ok(detailsService.patchDetails(clientId, request));
    }
}