package clm.client.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX        = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        var bearerToken = extractBearer(request);

        if (bearerToken.isPresent()) {
            log.info("Bearer token found, attempting to parse claims");
            var claims = tokenProvider.getClaims(bearerToken.get());
            if (claims.isPresent()) {
                authenticate(claims.get());
            } else {
                log.warn("Bearer token present but claims parsing failed");
            }
        } else {
            log.debug("No Bearer token found in request");
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractBearer(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(h -> h.startsWith(BEARER_PREFIX))
                .map(h -> h.substring(BEARER_PREFIX.length()));
    }

    private void authenticate(Claims claims) {
        var subject = claims.getSubject();
        if (!StringUtils.hasText(subject)) {
            log.debug("JWT subject missing — skipping authentication");
            return;
        }

        var userIdClaim = claims.get("userId");
        var principalName = userIdClaim != null ? userIdClaim.toString() : subject;

        var authorities = extractAuthorities(claims);
        var auth = new UsernamePasswordAuthenticationToken(principalName, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.debug("Authenticated request for subject '{}' with roles {}", principalName, authorities);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        var rolesClaim = claims.get("roles");
        List<String> roles = switch (rolesClaim) {
            case String rolesString -> List.of(rolesString.split(","));
            case Collection<?> collection -> collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
            default -> List.of();
        };

        return roles.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
