package clm.demo.simulation.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Generates long-lived JWT tokens for Gatling virtual users.
 *
 * Uses the same JJWT 0.12.x library already bundled with the application,
 * signing with HMAC-SHA256 and the shared secret read from the
 * {@code gatling.jwtSecret} system property (or the dev default).
 *
 * Tokens are created once at simulation startup – not per virtual user –
 * because the application only checks identity, not session state.
 */
public final class JwtTokenUtil {

    private JwtTokenUtil() {}

    /**
     * Generates a signed JWT valid for 7 days.
     *
     * @param secret  the HMAC-SHA256 secret (≥ 32 chars)
     * @param subject the user ID to embed as the token subject claim
     * @return compact JWT string
     */
    public static String generateToken(String secret, String subject) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + 7L * 24 * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    /** Convenience wrapper that prepends the "Bearer " prefix. */
    public static String bearerToken(String secret, String subject) {
        return "Bearer " + generateToken(secret, subject);
    }
}
