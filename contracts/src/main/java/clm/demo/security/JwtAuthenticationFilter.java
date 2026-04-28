package clm.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT Authentication Filter that validates JWT tokens from request headers
 * and sets the authentication context for secured endpoints.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && tokenProvider.validateToken(jwt)) {
                // Token is valid, set authentication context
                String subject = tokenProvider.getSubject(jwt);

                // Create authentication token with user details
                // You can add roles/authorities here if available in the JWT claims
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

                // Optionally extract roles from token if they exist
                Object roles = tokenProvider.getClaim(jwt, "roles");
                if (roles != null) {
                    if (roles instanceof List<?>) {
                        ((List<?>) roles).forEach(role ->
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
                        );
                    } else {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + roles.toString().toUpperCase()));
                    }
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT token validated for user: {}", subject);
            } else if (jwt != null) {
                log.warn("Invalid JWT token detected in request");
            }

        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * @param request HttpServletRequest
     * @return JWT token if found, null otherwise
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}

