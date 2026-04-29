package clm.client.demo.controllers;

import clm.client.demo.dtos.request.DetailsRequest;
import clm.client.demo.dtos.response.DetailsResponse;
import clm.client.demo.services.DetailsService;
import clm.client.demo.validation.ValidationGroups;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients/{clientId}/detalii")
@RequiredArgsConstructor
public class DetailsController {

    private final DetailsService detailsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<DetailsResponse> getDetails(@PathVariable Long clientId) {
        return ResponseEntity.ok(detailsService.getDetails(clientId));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<DetailsResponse> createOrReplaceDetails(
            @PathVariable Long clientId,
            @Validated(ValidationGroups.Create.class) @RequestBody DetailsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(detailsService.upsertDetails(clientId, request));
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<DetailsResponse> partialUpdateDetails(
            @PathVariable Long clientId,
            @Valid @RequestBody DetailsRequest request) {
        return ResponseEntity.ok(detailsService.patchDetails(clientId, request));
    }
}