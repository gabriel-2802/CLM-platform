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

@Slf4j
@Component
public class JwtTokenProvider {

    private final String jwtSecret;
    private SecretKey signingKey;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = Objects.requireNonNull(jwtSecret, "jwt.secret must not be null");
    }

    @PostConstruct
    void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // HS512 requires at least 64 bytes (512 bits)
        if (keyBytes.length < 64) {
            log.warn("""
                    jwt.secret should be at least 64 characters (512 bits) for HS512.
                    Current length: {} bytes. Token verification may fail.""".formatted(keyBytes.length));
        }
        // Create a key suitable for HS512
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        return parseClaims(token).isPresent();
    }

    public Optional<Claims> getClaims(String token) {
        return parseClaims(token);
    }

    public Optional<String> getSubject(String token) {
        return parseClaims(token).map(Claims::getSubject);
    }

    public Optional<Object> getClaim(String token, String claimName) {
        Objects.requireNonNull(claimName, "claimName must not be null");
        return parseClaims(token).map(c -> c.get(claimName));
    }

    private Optional<Claims> parseClaims(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            Claims payload = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(payload);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT parsing failed for token: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
