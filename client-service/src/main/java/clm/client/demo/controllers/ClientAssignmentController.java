package clm.client.demo.controllers;

import clm.client.demo.dtos.request.AssignmentRequest;
import clm.client.demo.dtos.response.AssignmentResponse;
import clm.client.demo.services.ClientAssignmentService;
import clm.client.demo.validation.ValidationGroups;
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
public class ClientAssignmentController {

    private final ClientAssignmentService assignmentService;

    @GetMapping("/clients/{clientId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> getAssignedUsersForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(assignmentService.getAssignedUsers(clientId));
    }

    @PutMapping("/clients/{clientId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> replaceUserAssignment(
            @PathVariable Long clientId,
            @Validated(ValidationGroups.Create.class) @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.replaceAssignments(clientId, request));
    }

    @PostMapping("/clients/{clientId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignUserToClient(
            @PathVariable Long clientId,
            @PathVariable Long userId) {
        assignmentService.assignUser(clientId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/clients/{clientId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUserFromClient(
            @PathVariable Long clientId,
            @PathVariable Long userId) {
        assignmentService.removeUser(clientId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Long>> getAssignedClientsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(assignmentService.getClientsForUser(userId));
    }
}