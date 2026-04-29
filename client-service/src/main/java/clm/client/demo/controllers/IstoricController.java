package clm.client.demo.controllers;

import clm.client.demo.dtos.request.IstoricRequest;
import clm.client.demo.dtos.response.IstoricResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/istorice")
public class IstoricController {

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<IstoricResponse>> listIstoriceByYears(@PathVariable Long clientId) {
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/{anul}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<IstoricResponse> getIstoricByYear(
            @PathVariable Long clientId,
            @PathVariable Integer anul) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/{anul}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<IstoricResponse> createOrReplaceIstoric(
            @PathVariable Long clientId,
            @PathVariable Integer anul,
            @RequestBody IstoricRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @DeleteMapping("/{anul}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteIstoric(
            @PathVariable Long clientId,
            @PathVariable Integer anul) {
        return ResponseEntity.noContent().build();
    }
}

