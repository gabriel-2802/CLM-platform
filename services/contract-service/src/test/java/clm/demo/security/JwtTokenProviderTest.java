package clm.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars-long!!";
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET);
        provider.init();
    }

    // ── validateToken ─────────────────────────────────────────────────────────

    @Test
    void should_return_true_for_valid_token() {
        String token = buildToken("user@test.com", SECRET, 60_000);
        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void should_return_false_for_expired_token() {
        String token = buildToken("user@test.com", SECRET, -1000); // already expired
        assertThat(provider.validateToken(token)).isFalse();
    }

    @Test
    void should_return_false_for_tampered_token() {
        String token = buildToken("user@test.com", SECRET, 60_000);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.validateToken(tampered)).isFalse();
    }

    @Test
    void should_return_false_for_null_token() {
        assertThat(provider.validateToken(null)).isFalse();
    }

    @Test
    void should_return_false_for_blank_token() {
        assertThat(provider.validateToken("   ")).isFalse();
    }

    @Test
    void should_return_false_for_token_signed_with_different_key() {
        String differentSecret = "completely-different-key-also-at-least-32-chars!!";
        String token = buildToken("user@test.com", differentSecret, 60_000);
        assertThat(provider.validateToken(token)).isFalse();
    }

    // ── getSubject ────────────────────────────────────────────────────────────

    @Test
    void should_return_subject_from_valid_token() {
        String token = buildToken("alice@example.com", SECRET, 60_000);
        Optional<String> subject = provider.getSubject(token);
        assertThat(subject).isPresent().contains("alice@example.com");
    }

    @Test
    void should_return_empty_for_invalid_token() {
        assertThat(provider.getSubject("not-a-jwt")).isEmpty();
    }

    @Test
    void should_return_empty_for_null_token() {
        assertThat(provider.getSubject(null)).isEmpty();
    }

    // ── getClaim ──────────────────────────────────────────────────────────────

    @Test
    void should_return_custom_claim_from_valid_token() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user@test.com")
                .claim("role", "ADMIN")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        Optional<Object> role = provider.getClaim(token, "role");
        assertThat(role).isPresent();
        assertThat(role.get()).isEqualTo("ADMIN");
    }

    @Test
    void should_return_empty_claim_for_invalid_token() {
        assertThat(provider.getClaim("invalid", "role")).isEmpty();
    }

    @Test
    void should_throw_when_claim_name_is_null() {
        String token = buildToken("user@test.com", SECRET, 60_000);
        assertThatThrownBy(() -> provider.getClaim(token, null))
                .isInstanceOf(NullPointerException.class);
    }

    // ── init validation ───────────────────────────────────────────────────────

    @Test
    void should_throw_during_init_when_secret_is_too_short() {
        JwtTokenProvider shortProvider = new JwtTokenProvider("short");
        assertThatThrownBy(shortProvider::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 characters");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String buildToken(String subject, String secret, long expiryMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key)
                .compact();
    }
}
