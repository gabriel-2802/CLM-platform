package clm.user.demo.controllers;

import clm.user.demo.dto.requests.ResetPasswordRequest;
import clm.user.demo.dto.requests.UpdateUserRequest;
import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static clm.user.demo.utils.Constants.DEFAULT_PAGE;
import static clm.user.demo.utils.Constants.DEFAULT_PAGE_SIZE;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users, roles, and account state")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Returns the profile of the currently authenticated user based on the Bearer token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authenticated user returned successfully"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token")
            }
    )
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getByEmail(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user by ID",
            description = "Returns the full profile of a user by their ID. Restricted to administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found and returned"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
                    @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN"),
                    @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
            }
    )
    public ResponseEntity<UserResponse> getById(
            @Parameter(description = "ID of the user to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List all users",
            description = "Returns a paginated list of all registered users, sorted by creation date descending. Restricted to administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated user list returned"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
                    @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN")
            }
    )
    public ResponseEntity<Page<UserResponse>> getAll(
            @Parameter(description = "Zero-based page index (default: 0)")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (default: 20)")
            @RequestParam(required = false) Integer size) {

        int p = Objects.nonNull(page) ? page : DEFAULT_PAGE;
        int s = Objects.nonNull(size) ? size : DEFAULT_PAGE_SIZE;

        return ResponseEntity.ok(userService.getAll(p, s));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update user",
            description = "Updates a user's email, name, and optionally their role. Restricted to administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
                    @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN"),
                    @ApiResponse(responseCode = "404", description = "No user exists with the given ID"),
                    @ApiResponse(responseCode = "409", description = "Email is already in use by another account")
            }
    )
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID of the user to update", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reset user password",
            description = "Replaces a user's password with the provided value. The new password is stored hashed. Restricted to administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
                    @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN"),
                    @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
            }
    )
    public ResponseEntity<UserResponse> resetPassword(
            @Parameter(description = "ID of the user whose password to reset", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(userService.resetPassword(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete user",
            description = "Permanently deletes a user account and all associated data. Restricted to administrators.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
                    @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN"),
                    @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
            }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true)
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/roles/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Grant admin role",
            description = "Adds ROLE_ADMIN to the specified user. Has no effect if the user already holds ROLE_ADMIN. Restricted to administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ROLE_ADMIN granted successfully"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
                    @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN"),
                    @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
            }
    )
    public ResponseEntity<UserResponse> grantAdmin(
            @Parameter(description = "ID of the user to promote", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.grantAdmin(id));
    }

    @DeleteMapping("/{id}/roles/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Revoke admin role",
            description = "Removes ROLE_ADMIN from the specified user. Has no effect if the user does not hold ROLE_ADMIN. Restricted to administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ROLE_ADMIN revoked successfully"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
                    @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN"),
                    @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
            }
    )
    public ResponseEntity<UserResponse> revokeAdmin(
            @Parameter(description = "ID of the user to demote", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.revokeAdmin(id));
    }

    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Set user enabled state",
            description = "Enables or disables a user account. Disabled users cannot authenticate. Restricted to administrators.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User enabled state updated successfully"),
                    @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token"),
                    @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN"),
                    @ApiResponse(responseCode = "404", description = "No user exists with the given ID")
            }
    )
    public ResponseEntity<UserResponse> setEnabled(
            @Parameter(description = "ID of the user to enable or disable", required = true)
            @PathVariable Long id,
            @Parameter(description = "Set to true to enable the account, false to disable", required = true)
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(userService.setEnabled(id, enabled));
    }
}