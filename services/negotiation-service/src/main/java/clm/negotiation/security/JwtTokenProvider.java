package clm.negotiation.security;

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
import java.util.Date;
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
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 characters. Set the JWT_SECRET environment variable.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateServiceToken() {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject("negotiation-service")
                .claim("role", "SERVICE")
                .issuedAt(new Date(now))
                .expiration(new Date(now + 3_600_000L))
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token) {
        return parseClaims(token).isPresent();
    }

    public Optional<String> getSubject(String token) {
        return parseClaims(token).map(Claims::getSubject);
    }

    private Optional<Claims> parseClaims(String token) {
        if (!StringUtils.hasText(token)) return Optional.empty();
        try {
            Claims payload = Jwts.parser()
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
