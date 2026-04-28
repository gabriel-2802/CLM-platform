package clm.user.demo.controllers;

import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.exceptions.ResourceNotFoundException;
import clm.user.demo.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    UserService userService;

    @InjectMocks
    UserController controller;

    MockMvc mockMvc;

    private static final UserResponse USER_RESPONSE = new UserResponse(
            1L,
            "user@test.com",
            "Test User",
            true,
            Set.of("ROLE_USER"),
            Instant.EPOCH
    );

    private static final UserResponse ADMIN_RESPONSE = new UserResponse(
            1L,
            "user@test.com",
            "Test User",
            true,
            Set.of("ROLE_USER", "ROLE_ADMIN"),
            Instant.EPOCH
    );

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();

        var userDetails = User.withUsername("user@test.com").password("x").roles("USER").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ─── GET /api/users/me ────────────────────────────────────────────────────

    @Test
    void getMe_returns200WithCurrentUser() throws Exception {
        given(userService.getByEmail("user@test.com")).willReturn(USER_RESPONSE);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    // ─── GET /api/users/{id} ──────────────────────────────────────────────────

    @Test
    void getById_exists_returns200() throws Exception {
        given(userService.getById(1L)).willReturn(USER_RESPONSE);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        given(userService.getById(99L)).willThrow(new ResourceNotFoundException("User not found: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/users ───────────────────────────────────────────────────────

    @Test
    void getAll_returns200WithList() throws Exception {
        given(userService.getAll()).willReturn(List.of(USER_RESPONSE));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("user@test.com"));
    }

    // ─── PUT /api/users/{id}/roles/admin ──────────────────────────────────────

    @Test
    void grantAdmin_returns200WithAdminRoles() throws Exception {
        given(userService.grantAdmin(1L)).willReturn(ADMIN_RESPONSE);

        mockMvc.perform(put("/api/users/1/roles/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray());
    }

    // ─── DELETE /api/users/{id}/roles/admin ───────────────────────────────────

    @Test
    void revokeAdmin_returns200() throws Exception {
        given(userService.revokeAdmin(1L)).willReturn(USER_RESPONSE);

        mockMvc.perform(delete("/api/users/1/roles/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    void revokeAdmin_notFound_returns404() throws Exception {
        given(userService.revokeAdmin(99L)).willThrow(new ResourceNotFoundException("User not found: 99"));

        mockMvc.perform(delete("/api/users/99/roles/admin"))
                .andExpect(status().isNotFound());
    }

    // ─── PATCH /api/users/{id}/enabled ────────────────────────────────────────

    @Test
    void setEnabled_false_returns200WithDisabledUser() throws Exception {
        var disabled = new UserResponse(
                1L,
                "user@test.com",
                "Test User",
                false,
                Set.of("ROLE_USER"),
                Instant.EPOCH
        );

        given(userService.setEnabled(1L, false)).willReturn(disabled);

        mockMvc.perform(patch("/api/users/1/enabled").param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void setEnabled_true_returns200WithEnabledUser() throws Exception {
        given(userService.setEnabled(1L, true)).willReturn(USER_RESPONSE);

        mockMvc.perform(patch("/api/users/1/enabled").param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void setEnabled_notFound_returns404() throws Exception {
        given(userService.setEnabled(99L, false)).willThrow(new ResourceNotFoundException("User not found: 99"));

        mockMvc.perform(patch("/api/users/99/enabled").param("enabled", "false"))
                .andExpect(status().isNotFound());
    }
}