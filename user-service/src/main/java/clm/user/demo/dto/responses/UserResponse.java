package clm.user.demo.dto.responses;

import clm.user.demo.models.User;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private boolean enabled;
    private Set<String> roles;
    private Instant createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
