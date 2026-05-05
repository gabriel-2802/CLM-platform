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
@Tag(name = "Clients", description = "Client management endpoints.")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/template-fields")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "List client fields available for template mapping")
    public ResponseEntity<List<String>> listTemplateFields() {
        return ResponseEntity.ok(clientService.listTemplateFields());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "List clients with optional filtering",
            description = "Returns paginated list of clients. USER role sees only their assigned clients.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully retrieved clients",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content)
            }
    )
    public ResponseEntity<Page<ClientResponse>> listClients(@ModelAttribute ClientListRequest request) {
        return ResponseEntity.ok(clientService.listClients(request, request.userId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Get a specific client by ID",
            description = "USER role must be assigned to this client.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "client found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<ClientResponse> getClientById(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Create a new client",
            responses = {
                    @ApiResponse(responseCode = "201", description = "client created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content)
            }
    )
    public ResponseEntity<ClientResponse> createClient(
            @Validated(ValidationGroups.Create.class) @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Full update of a client",
            responses = {
                    @ApiResponse(responseCode = "200", description = "client updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<ClientResponse> updateClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long id,
            @Validated(ValidationGroups.Create.class) @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(
            summary = "Partial update of a client",
            responses = {
                    @ApiResponse(responseCode = "200", description = "client partially updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))),
                    @ApiResponse(responseCode = "400", description = "validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<ClientResponse> partialUpdateClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.partialUpdateClient(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a client and all related data",
            responses = {
                    @ApiResponse(responseCode = "204", description = "client deleted", content = @Content),
                    @ApiResponse(responseCode = "401", description = "unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "forbidden", content = @Content),
                    @ApiResponse(responseCode = "404", description = "client not found", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "Client ID", required = true, example = "1")
            @PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}