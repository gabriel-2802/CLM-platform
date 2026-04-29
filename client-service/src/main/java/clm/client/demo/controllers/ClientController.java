package clm.client.demo.controllers;

import clm.client.demo.dtos.request.ClientRequest;
import clm.client.demo.dtos.request.ClientListRequest;
import clm.client.demo.dtos.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ClientResponse>> listClients(@ModelAttribute ClientListRequest request) {
        // USER sees only assigned ones
        // MANAGER/ADMIN see all
        Page<ClientResponse> emptyPage = new PageImpl<>(new ArrayList<>());
        return ResponseEntity.ok(emptyPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        // USER must be assigned
        return ResponseEntity.ok(null);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ClientResponse> createClient(@RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id,
            @RequestBody ClientRequest request) {
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<ClientResponse> partialUpdateClient(
            @PathVariable Long id,
            @RequestBody ClientRequest request) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        // Cascades all sub-entities
        return ResponseEntity.noContent().build();
    }
}


