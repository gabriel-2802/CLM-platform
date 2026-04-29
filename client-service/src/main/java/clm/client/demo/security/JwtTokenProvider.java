package clm.client.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * JWT Token Provider for validating and extracting claims from JWT tokens.
 * Supports tokens created by the auth service.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final String jwtSecret;
    private SecretKey signingKey;   // computed once, reused

    public JwtTokenProvider(
            @Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = Objects.requireNonNull(jwtSecret, "jwt.secret must not be null");
    }

    @PostConstruct
    void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 characters (256 bits) for HMAC-SHA256. " +
                    "Set the JWT_SECRET environment variable to a strong random value.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Returns {@code true} when the token signature and structure are valid.
     */
    public boolean validateToken(String token) {
        return parseClaims(token).isPresent();
    }

    /**
     * Returns the full {@link Claims} payload, or {@link Optional#empty()} if
     * the token is absent, blank, or fails validation.
     */
    public Optional<Claims> getClaims(String token) {
        return parseClaims(token);
    }

    /**
     * Returns the {@code sub} claim, or {@link Optional#empty()} on failure.
     */
    public Optional<String> getSubject(String token) {
        return parseClaims(token).map(Claims::getSubject);
    }

    /**
     * Returns the value of the named claim, or {@link Optional#empty()} on failure.
     *
     * @param claimName name of the claim to retrieve
     */
    public Optional<Object> getClaim(String token, String claimName) {
        Objects.requireNonNull(claimName, "claimName must not be null");
        return parseClaims(token).map(c -> c.get(claimName));
    }

    /**
     * Single parse point — avoids duplicating the parse/verify cycle.
     */
    private Optional<Claims> parseClaims(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            Claims payload = Jwts.parser()           // non-deprecated API (JJWT ≥ 0.12)
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(payload);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT parsing failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}