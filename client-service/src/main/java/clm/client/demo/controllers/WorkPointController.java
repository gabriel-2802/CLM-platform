package clm.client.demo.controllers;

import clm.client.demo.dtos.request.WorkPointRequest;
import clm.client.demo.dtos.response.WorkPointResponse;
import clm.client.demo.services.WorkPointService;
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
@RequestMapping("/api/clients/{clientId}/puncte-de-lucru")
@RequiredArgsConstructor
@Tag(name = "Work Points", description = "Work points (Puncte de Lucru) management - additional work locations for clients")
@SecurityRequirement(name = "bearerAuth")
public class WorkPointController {

    private final WorkPointService workPointService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "List all work points for a client",
            description = "Retrieves all work points (secondary locations) associated with a specific client."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Work points retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - USER not assigned to this client or insufficient role"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<List<WorkPointResponse>> listWorkPoints(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(workPointService.listWorkPoints(clientId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get a specific work point",
            description = "Retrieves a specific work point by ID for a client."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Work point retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkPointResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - USER not assigned to this client or insufficient role"),
            @ApiResponse(responseCode = "404", description = "Client or work point not found")
    })
    public ResponseEntity<WorkPointResponse> getWorkPoint(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Work point ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(workPointService.getWorkPoint(clientId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Create a new work point",
            description = "Creates a new work point (secondary location) for a client. Requires MANAGER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Work point created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkPointResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires MANAGER or ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<WorkPointResponse> createWorkPoint(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Work point creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkPointRequest.class))
            )
            @Validated(ValidationGroups.Create.class) @RequestBody WorkPointRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workPointService.createWorkPoint(clientId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Update a work point",
            description = "Updates an existing work point with full replacement. Requires MANAGER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Work point updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkPointResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires MANAGER or ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client or work point not found")
    })
    public ResponseEntity<WorkPointResponse> updateWorkPoint(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Work point ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Work point update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WorkPointRequest.class))
            )
            @Validated(ValidationGroups.Create.class) @RequestBody WorkPointRequest request) {
        return ResponseEntity.ok(workPointService.updateWorkPoint(clientId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a work point",
            description = "Deletes a specific work point for a client. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Work point deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client or work point not found")
    })
    public ResponseEntity<Void> deleteWorkPoint(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Work point ID", required = true, example = "1")
            @PathVariable Long id) {
        workPointService.deleteWorkPoint(clientId, id);
        return ResponseEntity.noContent().build();
    }
}