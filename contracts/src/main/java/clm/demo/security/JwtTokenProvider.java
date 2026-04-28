package clm.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT Token Provider for validating and extracting claims from JWT tokens.
 * Supports tokens created by the auth service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret:your-secret-key-change-this-in-properties}")
    private String jwtSecret;

    /**
     * Validate JWT token
     *
     * @param token JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation", e);
            return false;
        }
    }

    /**
     * Get claims from JWT token
     *
     * @param token JWT token string
     * @return Claims if token is valid, null otherwise
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Failed to extract claims from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract subject (username/userId) from token
     *
     * @param token JWT token string
     * @return subject if valid, null otherwise
     */
    public String getSubject(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Extract a specific claim from token
     *
     * @param token JWT token string
     * @param claimName name of the claim
     * @return claim value if valid, null otherwise
     */
    public Object getClaim(String token, String claimName) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get(claimName) : null;
    }

    /**
     * Get signing key from secret
     *
     * @return SecretKey for JWT signing/verification
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}

