package clm.client.demo.controllers;

import clm.client.demo.dtos.request.ClientListRequest;
import clm.client.demo.dtos.request.ClientRequest;
import clm.client.demo.dtos.response.ClientResponse;
import clm.client.demo.services.ClientService;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Client management endpoints. Supports filtering, partial updates, and cascading deletes.")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/template-fields")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "List client fields available for template mapping",
            description = "Returns the Romanian client column names that can be mapped into contract templates."
    )
    public ResponseEntity<List<String>> listTemplateFields() {
        return ResponseEntity.ok(clientService.listTemplateFields());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "List clients with optional filtering",
            description = "Returns paginated list of clients. USER role sees only their assigned clients. Supports filtering by status, type, and administrative area."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved clients",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<Page<ClientResponse>> listClients(@ModelAttribute ClientListRequest request) {
        return ResponseEntity.ok(clientService.listClients(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get a specific client by ID",
            description = "Retrieves client details. USER role must be assigned to this client."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - USER not assigned to this client or insufficient role"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientResponse> getClientById(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Create a new client",
            description = "Creates a new client with the provided details. Requires MANAGER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Client created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires MANAGER or ADMIN)")
    })
    public ResponseEntity<ClientResponse> createClient(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientRequest.class))
            )
            @Validated(ValidationGroups.Create.class) @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Update a client (full replacement)",
            description = "Performs a full update of the client. All required fields must be provided. Requires MANAGER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires MANAGER or ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientResponse> updateClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client update request (full replacement)",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientRequest.class))
            )
            @Validated(ValidationGroups.Create.class) @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Partially update a client",
            description = "Performs a partial update. Only provided fields are updated. Requires MANAGER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client partially updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires MANAGER or ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ClientResponse> partialUpdateClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Client partial update request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientRequest.class))
            )
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.partialUpdateClient(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a client",
            description = "Deletes a client and all related data (detalii, puncte de lucru, istoric, user assignments). Requires ADMIN role. Cascades to all child entities."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Client deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions (requires ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
