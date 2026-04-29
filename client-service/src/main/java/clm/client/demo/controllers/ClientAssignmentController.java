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
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "User-Client Assignment", description = "Manage user-to-client assignments. Controls which users have access to which clients.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class ClientAssignmentController {

    private final ClientAssignmentService assignmentService;

    @GetMapping("/clients/{clientId}/users")
    @Operation(
            summary = "Get users assigned to a client",
            description = "Retrieves all user IDs assigned to a specific client. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User assignments retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<AssignmentResponse> getAssignedUsersForClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId) {
        return ResponseEntity.ok(assignmentService.getAssignedUsers(clientId));
    }

    @PutMapping("/clients/{clientId}/users")
    @Operation(
            summary = "Replace all user assignments for a client",
            description = "Replaces the complete list of users assigned to a client. All existing assignments are removed and replaced with the provided list. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User assignments replaced successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssignmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<AssignmentResponse> replaceUserAssignment(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long clientId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Assignment request with list of user IDs",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AssignmentRequest.class))
            )
            @Validated(ValidationGroups.Create.class) @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.replaceAssignments(clientId, request));
    }

    @PostMapping("/clients/{clientId}/users/{userId}")
    @Operation(
            summary = "Assign a user to a client",
            description = "Assigns a specific user to a specific client. If already assigned, this is a no-op. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user or client ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
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
            description = "Removes a specific user's access to a specific client. If not assigned, this is a no-op. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user or client ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
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
            description = "Retrieves all client IDs assigned to a specific user (reverse lookup). Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client assignments retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<List<Long>> getAssignedClientsForUser(
            @Parameter(description = "User ID", required = true, example = "42")
            @PathVariable Long userId) {
        return ResponseEntity.ok(assignmentService.getClientsForUser(userId));
    }
}