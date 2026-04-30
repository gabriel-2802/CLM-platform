package clm.user.demo.controllers;

import clm.user.demo.dto.requests.LoginRequest;
import clm.user.demo.dto.requests.RegisterRequest;
import clm.user.demo.dto.responses.AuthResponse;
import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.exceptions.GlobalExceptionHandler;
import clm.user.demo.exceptions.exceptions.DuplicateEmailException;
import clm.user.demo.exceptions.exceptions.InvalidCredentialsException;
import clm.user.demo.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    @Mock
    AuthService authService;

    @InjectMocks
    AuthController controller;

    MockMvc mockMvc;

    // Added JavaTimeModule to handle Java 8 dates (Instant)
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final UserResponse USER_RESPONSE = new UserResponse(
            1L,
            "user@test.com",
            "Test User",
            true,
            Set.of("ROLE_USER"),
            Instant.EPOCH
    );

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.type").value("https://api.clm-user.demo/errors/validation-failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registerBody("user@test.com", "short", "Name", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.type").value("https://api.clm-user.demo/errors/validation-failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        given(authService.register(any())).willThrow(new DuplicateEmailException());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                registerBody("user@test.com", "Password1!", "Name", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email already registered"))
                .andExpect(jsonPath("$.type").value("https://api.clm-user.demo/errors/duplicate-email"))
                .andExpect(jsonPath("$.timestamp").exists());
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
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"))
                .andExpect(jsonPath("$.type").value("https://api.clm-user.demo/errors/invalid-credentials"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void login_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.type").value("https://api.clm-user.demo/errors/validation-failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray());
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    // Updated to use the record constructors instead of setters
    private static RegisterRequest registerBody(String email, String password, String name, String adminCode) {
        return new RegisterRequest(email, password, name, adminCode);
    }

    private static LoginRequest loginBody(String email, String password) {
        return new LoginRequest(email, password);
    }
}