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
@RequestMapping("/api/clients/{clientId}/histories")
@RequiredArgsConstructor
@Tag(name = "Histories", description = "Client financial history management.")
@SecurityRequirement(name = "bearerAuth")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "List all history records for a client",
            responses = {
                    @ApiResponse(responseCode = "200", description = "histories retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<List<HistoryResponse>> listHistory(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(historyService.listHistory(clientId));
    }

    @GetMapping("/{year}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get history record for a specific year",
            responses = {
                    @ApiResponse(responseCode = "200", description = "history retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = HistoryResponse.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "history not found", content = @Content)
            }
    )
    public ResponseEntity<HistoryResponse> getHistory(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Year", required = true, example = "2024")
            @PathVariable Integer year) {
        return ResponseEntity.ok(historyService.getHistory(clientId, year));
    }

    @PutMapping("/{year}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Create or replace history record for a specific year",
            responses = {
                    @ApiResponse(responseCode = "201", description = "history created or replaced",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = HistoryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<HistoryResponse> createOrReplaceHistory(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Year", required = true, example = "2024")
            @PathVariable Integer year,
            @Validated(ValidationGroups.Create.class) @RequestBody HistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(historyService.upsertHistory(clientId, year, request));
    }

    @DeleteMapping("/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete history record for a specific year",
            responses = {
                    @ApiResponse(responseCode = "204", description = "history deleted", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "history not found", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteHistory(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Year", required = true, example = "2024")
            @PathVariable Integer year) {
        historyService.deleteHistory(clientId, year);
        return ResponseEntity.noContent().build();
    }
}