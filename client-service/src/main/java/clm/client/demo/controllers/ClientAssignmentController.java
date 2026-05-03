package clm.client.demo.controllers;

import clm.client.demo.dtos.request.AssignmentRequest;
import clm.client.demo.dtos.response.AssignmentResponse;
import clm.client.demo.services.ClientAssignmentService;
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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "User-Client Assignment", description = "Manage user-to-client assignments.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class ClientAssignmentController {

    private final ClientAssignmentService assignmentService;

    @GetMapping("/clients/{clientId}/users")
    @Operation(
            summary = "Get users assigned to a client",
            responses = {
                    @ApiResponse(responseCode = "200", description = "assignments retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponse.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<AssignmentResponse> getAssignedUsersForClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(assignmentService.getAssignedUsers(clientId));
    }

    @PutMapping("/clients/{clientId}/users")
    @Operation(
            summary = "Replace all user assignments for a client",
            description = "Removes all existing assignments and replaces them with the provided list.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "assignments replaced",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<AssignmentResponse> replaceUserAssignment(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Validated(ValidationGroups.Create.class) @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.replaceAssignments(clientId, request));
    }

    @PostMapping("/clients/{clientId}/users/{userId}")
    @Operation(
            summary = "Assign a user to a client",
            description = "If already assigned, this is a no-op.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "user assigned", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<Void> assignUserToClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "User ID", required = true, example = "42")
            @PathVariable Long userId) {
        assignmentService.assignUser(clientId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/clients/{clientId}/users/{userId}")
    @Operation(
            summary = "Remove a user from a client",
            description = "If not assigned, this is a no-op.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "user removed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<Void> removeUserFromClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "User ID", required = true, example = "42")
            @PathVariable Long userId) {
        assignmentService.removeUser(clientId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/clients")
    @Operation(
            summary = "Get clients assigned to a user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "client assignments retrieved",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content)
            }
    )
    public ResponseEntity<List<Long>> getAssignedClientsForUser(
            @Parameter(description = "User ID", required = true, example = "42")
            @PathVariable Long userId) {
        return ResponseEntity.ok(assignmentService.getClientsForUser(userId));
    }
}