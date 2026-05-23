package clm.client.demo.config;

import clm.client.demo.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Test security config for @WebMvcTest slices.
 *
 * Uses a custom SecurityContextRepository that captures the security context
 * set by @WithMockUser from SecurityContextHolder at the start of each request,
 * before SecurityContextHolderFilter replaces it via setDeferredContext().
 * This is required because Spring Security 7 always uses setDeferredContext(),
 * which would otherwise overwrite the test context with an empty session context.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class WebMvcTestSecurityConfig {

    @Bean
    JwtTokenProvider jwtTokenProvider() {
        return Mockito.mock(JwtTokenProvider.class);
    }

    @Bean
    SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .securityContext(ctx -> ctx
                        .securityContextRepository(new HolderSnapshotRepository()))
                .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
                .build();
    }

    private static class HolderSnapshotRepository implements SecurityContextRepository {

        @Override
        public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
            return SecurityContextHolder.getContext();
        }

        @Override
        public DeferredSecurityContext loadDeferredContext(HttpServletRequest request) {
            SecurityContext snapshot = SecurityContextHolder.getContext();
            return new DeferredSecurityContext() {
                @Override
                public SecurityContext get() {
                    return snapshot;
                }

                @Override
                public boolean isGenerated() {
                    return snapshot.getAuthentication() == null;
                }
            };
        }

        @Override
        public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {}

        @Override
        public boolean containsContext(HttpServletRequest request) {
            return SecurityContextHolder.getContext().getAuthentication() != null;
        }
    }
}
