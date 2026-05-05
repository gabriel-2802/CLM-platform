package clm.client.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        var name = authentication.getName();
        if (!StringUtils.hasText(name)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(name));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    public static boolean isUserOnly() {
        return hasRole("ROLE_USER") && !hasRole("ROLE_ADMIN") && !hasRole("ROLE_MANAGER");
    }
}