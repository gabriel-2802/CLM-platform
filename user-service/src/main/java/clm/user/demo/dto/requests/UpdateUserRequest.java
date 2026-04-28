package clm.user.demo.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank @Email String email,
        @Size(max = 255) String name,
        String role
) {}
