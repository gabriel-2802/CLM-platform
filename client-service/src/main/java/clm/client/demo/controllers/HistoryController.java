package clm.client.demo.controllers;

import clm.client.demo.dtos.request.HistoryRequest;
import clm.client.demo.dtos.response.HistoryResponse;
import clm.client.demo.services.HistoryService;
import clm.client.demo.validation.ValidationGroups;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/istorice")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<HistoryResponse>> listHistory(@PathVariable Long clientId) {
        return ResponseEntity.ok(historyService.listHistory(clientId));
    }

    @GetMapping("/{anul}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<HistoryResponse> getHistory(
            @PathVariable Long clientId,
            @PathVariable Integer anul) {
        return ResponseEntity.ok(historyService.getHistory(clientId, anul));
    }

    @PutMapping("/{anul}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<HistoryResponse> createOrReplaceHistory(
            @PathVariable Long clientId,
            @PathVariable Integer anul,
            @Validated(ValidationGroups.Create.class) @RequestBody HistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(historyService.upsertHistory(clientId, anul, request));
    }

    @DeleteMapping("/{anul}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHistory(
            @PathVariable Long clientId,
            @PathVariable Integer anul) {
        historyService.deleteHistory(clientId, anul);
        return ResponseEntity.noContent().build();
    }
}