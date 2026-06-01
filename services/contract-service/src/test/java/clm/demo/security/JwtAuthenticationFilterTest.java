package clm.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtTokenProvider tokenProvider;
    @Mock HttpServletRequest  request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    @InjectMocks JwtAuthenticationFilter filter;

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearContextAfter() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_populate_security_context_when_valid_bearer_token_present() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.token.here");
        when(tokenProvider.getSubject("valid.token.here"))
                .thenReturn(Optional.of("user@example.com"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo("user@example.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_not_populate_security_context_when_no_authorization_header() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).getSubject(anyString());
    }

    @Test
    void should_not_populate_security_context_when_bearer_token_invalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(tokenProvider.getSubject("bad.token")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void should_not_populate_context_when_header_is_not_bearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).getSubject(anyString());
    }

    @Test
    void should_skip_filter_for_actuator_paths() throws Exception {
        when(request.getServletPath()).thenReturn("/actuator/health");

        // shouldNotFilter returns true for /actuator, so doFilterInternal is not called
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void should_not_skip_filter_for_api_paths() throws Exception {
        when(request.getServletPath()).thenReturn("/api/contracts/1");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void should_always_call_filter_chain_regardless_of_auth_outcome() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
