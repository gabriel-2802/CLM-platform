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
@RequestMapping("/api/clients/{clientId}/work-points")
@RequiredArgsConstructor
@Tag(name = "Work Points", description = "Work point management — additional work locations for clients.")
@SecurityRequirement(name = "bearerAuth")
public class WorkPointController {

    private final WorkPointService workPointService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "List all work points for a client",
            responses = {
                    @ApiResponse(responseCode = "200", description = "work points retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<List<WorkPointResponse>> listWorkPoints(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(workPointService.listWorkPoints(clientId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get a specific work point",
            responses = {
                    @ApiResponse(responseCode = "200", description = "work point retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkPointResponse.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client or work point not found", content = @Content)
            }
    )
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
            responses = {
                    @ApiResponse(responseCode = "201", description = "work point created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkPointResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<WorkPointResponse> createWorkPoint(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Validated(ValidationGroups.Create.class) @RequestBody WorkPointRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workPointService.createWorkPoint(clientId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Full update of a work point",
            responses = {
                    @ApiResponse(responseCode = "200", description = "work point updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WorkPointResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client or work point not found", content = @Content)
            }
    )
    public ResponseEntity<WorkPointResponse> updateWorkPoint(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Work point ID", required = true, example = "1")
            @PathVariable Long id,
            @Validated(ValidationGroups.Create.class) @RequestBody WorkPointRequest request) {
        return ResponseEntity.ok(workPointService.updateWorkPoint(clientId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a work point",
            responses = {
                    @ApiResponse(responseCode = "204", description = "work point deleted", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client or work point not found", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteWorkPoint(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Work point ID", required = true, example = "1")
            @PathVariable Long id) {
        workPointService.deleteWorkPoint(clientId, id);
        return ResponseEntity.noContent().build();
    }
}