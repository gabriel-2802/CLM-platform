package clm.client.demo.controllers;

import clm.client.demo.dtos.request.HistoryRequest;
import clm.client.demo.dtos.response.HistoryResponse;
import clm.client.demo.services.HistoryService;
import clm.client.demo.validation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/istorice")
@RequiredArgsConstructor
@Tag(name = "History", description = "Client historical data management (Istoric) - year-by-year financial and audit information")
@SecurityRequirement(name = "bearerAuth")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "List all history records for a client",
            description = "Retrieves all historical records (by year) for a specific client."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History records retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - USER not assigned to this client or insufficient role"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<List<HistoryResponse>> listHistory(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(historyService.listHistory(clientId));
    }

    @GetMapping("/{anul}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get history for a specific year",
            description = "Retrieves historical data for a specific year for a client."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History record retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HistoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - USER not assigned to this client or insufficient role"),
            @ApiResponse(responseCode = "404", description = "Client or history record not found")
    })
    public ResponseEntity<HistoryResponse> getHistory(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Year (anul)", required = true, example = "2024")
            @PathVariable Integer anul) {
        return ResponseEntity.ok(historyService.getHistory(clientId, anul));
    }

    @PutMapping("/{anul}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Create or replace history record for a year",
            description = "Creates or replaces the history record for a specific year. If a record already exists for this year, it will be replaced. Requires MANAGER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "History record created or replaced successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HistoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires MANAGER or ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<HistoryResponse> createOrReplaceHistory(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Year (anul)", required = true, example = "2024")
            @PathVariable Integer anul,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "History creation/replacement request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = HistoryRequest.class))
            )
            @Validated(ValidationGroups.Create.class) @RequestBody HistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(historyService.upsertHistory(clientId, anul, request));
    }

    @DeleteMapping("/{anul}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete history record for a year",
            description = "Deletes the history record for a specific year for a client. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "History record deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client or history record not found")
    })
    public ResponseEntity<Void> deleteHistory(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Year (anul)", required = true, example = "2024")
            @PathVariable Integer anul) {
        historyService.deleteHistory(clientId, anul);
        return ResponseEntity.noContent().build();
    }
}