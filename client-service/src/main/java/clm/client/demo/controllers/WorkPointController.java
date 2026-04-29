package clm.client.demo.controllers;

import clm.client.demo.dtos.request.WorkPointRequest;
import clm.client.demo.dtos.response.WorkPointResponse;
import clm.client.demo.services.WorkPointService;
import clm.client.demo.validation.ValidationGroups;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/puncte-de-lucru")
@RequiredArgsConstructor
public class WorkPointController {

    private final WorkPointService workPointService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<WorkPointResponse>> listWorkPoints(@PathVariable Long clientId) {
        return ResponseEntity.ok(workPointService.listWorkPoints(clientId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<WorkPointResponse> getWorkPoint(
            @PathVariable Long clientId,
            @PathVariable Long id) {
        return ResponseEntity.ok(workPointService.getWorkPoint(clientId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<WorkPointResponse> createWorkPoint(
            @PathVariable Long clientId,
            @Validated(ValidationGroups.Create.class) @RequestBody WorkPointRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workPointService.createWorkPoint(clientId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<WorkPointResponse> updateWorkPoint(
            @PathVariable Long clientId,
            @PathVariable Long id,
            @Validated(ValidationGroups.Create.class) @RequestBody WorkPointRequest request) {
        return ResponseEntity.ok(workPointService.updateWorkPoint(clientId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkPoint(
            @PathVariable Long clientId,
            @PathVariable Long id) {
        workPointService.deleteWorkPoint(clientId, id);
        return ResponseEntity.noContent().build();
    }
}