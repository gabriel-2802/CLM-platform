package clm.user.demo.services;

import clm.user.demo.dto.requests.LoginRequest;
import clm.user.demo.dto.requests.RegisterRequest;
import clm.user.demo.dto.responses.AuthResponse;
import clm.user.demo.exceptions.DuplicateEmailException;
import clm.user.demo.exceptions.InvalidCredentialsException;
import clm.user.demo.models.Role;
import clm.user.demo.models.User;
import clm.user.demo.repositories.RoleRepository;
import clm.user.demo.repositories.UserRepository;
import clm.user.demo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository        userRepository;
    @Mock RoleRepository        roleRepository;
    @Mock PasswordEncoder       passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtTokenProvider      tokenProvider;
    @Mock UserDetailsService    userDetailsService;

    @InjectMocks AuthService authService;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "adminRegisterCode", "devcode123");
        ReflectionTestUtils.setField(authService, "jwtExpirationMs",   3_600_000L);

        userRole = role(1, "ROLE_USER");
        adminRole = role(2, "ROLE_ADMIN");
    }

    // ─── register ─────────────────────────────────────────────────────────────

    @Test
    void register_validRequest_returnsTokenAndUser() {
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(userRole));
        given(passwordEncoder.encode("Password1!")).willReturn("encoded");
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UserDetails details = stubDetails("ROLE_USER");
        given(userDetailsService.loadUserByUsername("new@test.com")).willReturn(details);
        given(tokenProvider.generateToken(details)).willReturn("jwt.token");

        AuthResponse resp = authService.register(registerRequest("new@test.com", "Password1!", null));

        assertThat(resp.getToken()).isEqualTo("jwt.token");
        assertThat(resp.getTokenType()).isEqualTo("Bearer");
        assertThat(resp.getUser().getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void register_duplicateEmail_throwsDuplicateEmailException() {
        given(userRepository.existsByEmail("dup@test.com")).willReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest("dup@test.com", "Password1!", null)))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void register_withValidAdminCode_grantsAdminRole() {
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(userRole));
        given(roleRepository.findByName("ROLE_ADMIN")).willReturn(Optional.of(adminRole));
        given(passwordEncoder.encode(any())).willReturn("encoded");

        var captor = ArgumentCaptor.forClass(User.class);
        given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        UserDetails details = stubDetails("ROLE_USER", "ROLE_ADMIN");
        given(userDetailsService.loadUserByUsername(any())).willReturn(details);
        given(tokenProvider.generateToken(any())).willReturn("jwt.admin");

        authService.register(registerRequest("adm@test.com", "Password1!", "devcode123"));

        assertThat(captor.getValue().getRoles())
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void register_withWrongAdminCode_doesNotGrantAdminRole() {
        given(userRepository.existsByEmail(any())).willReturn(false);
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(userRole));
        given(passwordEncoder.encode(any())).willReturn("encoded");

        var captor = ArgumentCaptor.forClass(User.class);
        given(userRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        UserDetails details = stubDetails("ROLE_USER");
        given(userDetailsService.loadUserByUsername(any())).willReturn(details);
        given(tokenProvider.generateToken(any())).willReturn("jwt");

        authService.register(registerRequest("usr@test.com", "Password1!", "wrongcode"));

        assertThat(captor.getValue().getRoles())
                .extracting(Role::getName)
                .containsExactly("ROLE_USER");
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returnsTokenAndUser() {
        given(authenticationManager.authenticate(any())).willReturn(mock(Authentication.class));

        UserDetails details = stubDetails("ROLE_USER");
        given(userDetailsService.loadUserByUsername("usr@test.com")).willReturn(details);

        User user = user("usr@test.com");
        given(userRepository.findByEmail("usr@test.com")).willReturn(Optional.of(user));
        given(tokenProvider.generateToken(details)).willReturn("jwt.login");

        AuthResponse resp = authService.login(loginRequest("usr@test.com", "Password1!"));

        assertThat(resp.getToken()).isEqualTo("jwt.login");
        assertThat(resp.getUser().getEmail()).isEqualTo("usr@test.com");
    }

    @Test
    void login_badCredentials_throwsInvalidCredentialsException() {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest("usr@test.com", "WrongPass!")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private static RegisterRequest registerRequest(String email, String password, String adminCode) {
        var r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword(password);
        r.setName("Test User");
        r.setAdminCode(adminCode);
        return r;
    }

    private static LoginRequest loginRequest(String email, String password) {
        var r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    private static UserDetails stubDetails(String... roles) {
        // getAuthorities() is never called directly — tokenProvider is mocked,
        // so JwtTokenProvider.generateToken never runs and doesn't need the stub
        return mock(UserDetails.class);
    }

    private static Role role(int id, String name) {
        var r = new Role();
        r.setId(id);
        r.setName(name);
        return r;
    }

    private static User user(String email) {
        var u = new User();
        u.setId(1L);
        u.setEmail(email);
        u.setName("Test User");
        u.setEnabled(true);
        return u;
    }
}
