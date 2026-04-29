package clm.client.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Validates the Bearer JWT on every request and populates the
 * SecurityContext so downstream filters and controllers can rely on
 * {@code SecurityContextHolder.getContext().getAuthentication()}.
 *
 * Role-based authorisation is intentionally absent — this service
 * only needs to verify identity, not permissions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX        = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        extractBearer(request)
                .flatMap(tokenProvider::getSubject)
                .ifPresentOrElse(
                        subject -> {
                            var auth = new UsernamePasswordAuthenticationToken(
                                    subject, null, List.of());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            log.debug("Authenticated request for subject '{}'", subject);
                        },
                        () -> log.debug("No valid Bearer token — request proceeds unauthenticated")
                );

        filterChain.doFilter(request, response);
    }

    /**
     * Pulls the raw JWT out of the Authorization header, returning
     * {@link Optional#empty()} when the header is absent or
     * not a Bearer token.
     */
    private Optional<String> extractBearer(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(h -> h.startsWith(BEARER_PREFIX))
                .map(h -> h.substring(BEARER_PREFIX.length()));
    }
}