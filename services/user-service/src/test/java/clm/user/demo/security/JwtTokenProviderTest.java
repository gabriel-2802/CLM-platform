package clm.user.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-characters-long";
    private static final long   EXPIRY = 3_600_000L;

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRY);
        tokenProvider.init();
    }

    @Test
    void generateToken_returnsNonBlankJwt() {
        String token = tokenProvider.generateToken(userDetails("test@example.com", "ROLE_USER"));
        assertThat(token).isNotBlank().contains(".");
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = tokenProvider.generateToken(userDetails("test@example.com", "ROLE_USER"));
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_tampered_returnsFalse() {
        String token = tokenProvider.generateToken(userDetails("test@example.com", "ROLE_USER"));
        assertThat(tokenProvider.validateToken(token + "tampered")).isFalse();
    }

    @Test
    void validateToken_blank_returnsFalse() {
        assertThat(tokenProvider.validateToken("")).isFalse();
        assertThat(tokenProvider.validateToken(null)).isFalse();
        assertThat(tokenProvider.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    void getSubject_validToken_returnsEmail() {
        String token = tokenProvider.generateToken(userDetails("alice@example.com", "ROLE_USER"));
        assertThat(tokenProvider.getSubject(token)).contains("alice@example.com");
    }

    @Test
    void getSubject_invalidToken_returnsEmpty() {
        assertThat(tokenProvider.getSubject("garbage")).isEmpty();
    }

    @Test
    void init_secretTooShort_throwsIllegalStateException() {
        var provider = new JwtTokenProvider("tooshort", EXPIRY);
        assertThatThrownBy(provider::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 characters");
    }

    @Test
    void getClaims_validToken_containsRoles() {
        String token = tokenProvider.generateToken(userDetails("test@example.com", "ROLE_ADMIN"));
        var claims = tokenProvider.getClaims(token);
        assertThat(claims).isPresent();
        assertThat(claims.get().get("roles")).isNotNull();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static org.springframework.security.core.userdetails.UserDetails userDetails(
            String email, String role) {
        return User.withUsername(email).password("x").roles(role.replace("ROLE_", "")).build();
    }
}
// test
