package clm.user.demo.controllers;

import clm.user.demo.dto.requests.LoginRequest;
import clm.user.demo.dto.requests.RegisterRequest;
import clm.user.demo.dto.responses.AuthResponse;
import clm.user.demo.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints for user registration and authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with ROLE_USER. Optionally grants ROLE_ADMIN if a valid admin code is provided.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully, JWT returned"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body"),
                    @ApiResponse(responseCode = "409", description = "Email is already registered")
            }
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Validates credentials and returns a signed JWT token along with the authenticated user's profile.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful, JWT returned"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body"),
                    @ApiResponse(responseCode = "401", description = "Invalid email or password")
            }
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}