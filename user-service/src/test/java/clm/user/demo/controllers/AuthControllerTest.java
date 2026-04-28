package clm.user.demo.controllers;

import clm.user.demo.dto.requests.LoginRequest;
import clm.user.demo.dto.requests.RegisterRequest;
import clm.user.demo.dto.responses.AuthResponse;
import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.exceptions.DuplicateEmailException;
import clm.user.demo.exceptions.InvalidCredentialsException;
import clm.user.demo.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock    AuthService     authService;
    @InjectMocks AuthController controller;

    MockMvc        mockMvc;
    ObjectMapper   objectMapper = new ObjectMapper();

    private static final UserResponse USER_RESPONSE = UserResponse.builder()
            .id(1L).email("user@test.com").name("Test User")
            .enabled(true).roles(Set.of("ROLE_USER")).createdAt(Instant.EPOCH)
            .build();

    private static final AuthResponse AUTH_RESPONSE =
            AuthResponse.of("jwt.test.token", 3_600_000L, USER_RESPONSE);

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    // ─── POST /api/auth/register ───────────────────────────────────────────────

    @Test
    void register_validRequest_returns201WithToken() throws Exception {
        given(authService.register(any())).willReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registerBody("user@test.com", "Password1!", "Test User", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt.test.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("user@test.com"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registerBody("not-an-email", "Password1!", "Name", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registerBody("user@test.com", "short", "Name", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        given(authService.register(any())).willThrow(new DuplicateEmailException("user@test.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registerBody("user@test.com", "Password1!", "Name", null))))
                .andExpect(status().isConflict());
    }

    // ─── POST /api/auth/login ─────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        given(authService.login(any())).willReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                loginBody("user@test.com", "Password1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.test.token"))
                .andExpect(jsonPath("$.user.email").value("user@test.com"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        given(authService.login(any())).willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                loginBody("user@test.com", "WrongPass1!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private static RegisterRequest registerBody(String email, String password, String name, String adminCode) {
        var r = new RegisterRequest();
        r.setEmail(email);
        r.setPassword(password);
        r.setName(name);
        r.setAdminCode(adminCode);
        return r;
    }

    private static LoginRequest loginBody(String email, String password) {
        var r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }
}
