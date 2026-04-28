package clm.user.demo.controllers;

import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getByEmail(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a user by ID (admin only)")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users (admin only)")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PutMapping("/{id}/roles/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Grant ROLE_ADMIN to a user (admin only)")
    public ResponseEntity<UserResponse> grantAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.grantAdmin(id));
    }

    @DeleteMapping("/{id}/roles/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke ROLE_ADMIN from a user (admin only)")
    public ResponseEntity<UserResponse> revokeAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.revokeAdmin(id));
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable or disable a user (admin only)")
    public ResponseEntity<UserResponse> setEnabled(@PathVariable Long id,
                                                    @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.setEnabled(id, enabled));
    }
}
