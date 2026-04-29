package clm.client.demo.controllers;

import clm.client.demo.dtos.request.DetaliiRequest;
import clm.client.demo.dtos.response.DetaliiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients/{clientId}/detalii")
public class DetaliiController {

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<DetaliiResponse> getDetalii(@PathVariable Long clientId) {
        return ResponseEntity.ok(null);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<DetaliiResponse> createOrReplaceDetalii(
            @PathVariable Long clientId,
            @RequestBody DetaliiRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<DetaliiResponse> partialUpdateDetalii(
            @PathVariable Long clientId,
            @RequestBody DetaliiRequest request) {
        return ResponseEntity.ok(null);
    }
}

