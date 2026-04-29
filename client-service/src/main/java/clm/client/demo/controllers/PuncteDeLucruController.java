package clm.client.demo.controllers;

import clm.client.demo.dtos.request.PunctDeLucruRequest;
import clm.client.demo.dtos.response.PunctDeLucruResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/puncte-de-lucru")
public class PuncteDeLucruController {

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<PunctDeLucruResponse>> listPuncteDeLucru(@PathVariable Long clientId) {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PunctDeLucruResponse> getPunctDeLucru(
            @PathVariable Long clientId,
            @PathVariable Long id) {
        return ResponseEntity.ok(null);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<PunctDeLucruResponse> createPunctDeLucru(
            @PathVariable Long clientId,
            @RequestBody PunctDeLucruRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<PunctDeLucruResponse> updatePunctDeLucru(
            @PathVariable Long clientId,
            @PathVariable Long id,
            @RequestBody PunctDeLucruRequest request) {
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePunctDeLucru(
            @PathVariable Long clientId,
            @PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}

