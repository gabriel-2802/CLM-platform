package clm.notifications.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

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
                .subject("notification-service")
                .claim("role", "SERVICE")
                .issuedAt(new Date(now))
                .expiration(new Date(now + 3_600_000L))
                .signWith(signingKey)
                .compact();
    }
}
