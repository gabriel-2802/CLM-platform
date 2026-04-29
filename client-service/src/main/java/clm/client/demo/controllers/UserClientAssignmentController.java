package clm.client.demo.controllers;

import clm.client.demo.dtos.request.AssignmentRequest;
import clm.client.demo.dtos.response.AssignmentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserClientAssignmentController {

    // User-Client Assignments

    @GetMapping("/clients/{clientId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> getAssignedUsersForClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(null);
    }

    @PutMapping("/clients/{clientId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AssignmentResponse> replaceUserAssignment(
            @PathVariable Long clientId,
            @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(null);
    }

    @PostMapping("/clients/{clientId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignUserToClient(
            @PathVariable Long clientId,
            @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/clients/{clientId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUserFromClient(
            @PathVariable Long clientId,
            @PathVariable Long userId) {
        return ResponseEntity.noContent().build();
    }

    // Reverse lookup: clients assigned to a user

    @GetMapping("/users/{userId}/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Long>> getAssignedClientsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new ArrayList<>());
    }
}

