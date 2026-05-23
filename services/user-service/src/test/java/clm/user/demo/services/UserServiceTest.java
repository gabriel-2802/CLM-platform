package clm.user.demo.services;

import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.exceptions.exceptions.ResourceNotFoundException;
import clm.user.demo.models.Role;
import clm.user.demo.models.User;
import clm.user.demo.repositories.RoleRepository;
import clm.user.demo.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private User   testUser;
    private Role   userRole;
    private Role   adminRole;

    @BeforeEach
    void setUp() {
        userRole  = role(1, "ROLE_USER");
        adminRole = role(2, "ROLE_ADMIN");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setName("Test User");
        testUser.setEnabled(true);
        testUser.getRoles().add(userRole);
    }

    // ─── reads ────────────────────────────────────────────────────────────────

    @Test
    void getByEmail_found_returnsResponse() {
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(testUser));

        UserResponse resp = userService.getByEmail("user@test.com");

        assertThat(resp.email()).isEqualTo("user@test.com");
        assertThat(resp.roles()).containsExactly("ROLE_USER");
    }

    @Test
    void getByEmail_notFound_throwsResourceNotFoundException() {
        given(userRepository.findByEmail("ghost@test.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("ghost@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_found_returnsResponse() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        UserResponse resp = userService.getById(1L);

        assertThat(resp.id()).isEqualTo(1L);
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_returnsMappedList() {
        given(userRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(testUser)));

        var page = userService.getAll(0, 20);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).email()).isEqualTo("user@test.com");
    }

    // ─── role management ──────────────────────────────────────────────────────

    @Test
    void grantAdmin_addsAdminRoleToUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(roleRepository.findByName("ROLE_ADMIN")).willReturn(Optional.of(adminRole));
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UserResponse resp = userService.grantAdmin(1L);

        assertThat(resp.roles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void grantAdmin_alreadyAdmin_isIdempotent() {
        testUser.getRoles().add(adminRole);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(roleRepository.findByName("ROLE_ADMIN")).willReturn(Optional.of(adminRole));
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UserResponse resp = userService.grantAdmin(1L);

        // Set semantics — still has ROLE_ADMIN exactly once
        assertThat(resp.roles()).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void revokeAdmin_removesAdminRole() {
        testUser.getRoles().add(adminRole);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UserResponse resp = userService.revokeAdmin(1L);

        assertThat(resp.roles()).containsExactly("ROLE_USER");
        assertThat(resp.roles()).doesNotContain("ROLE_ADMIN");
    }

    @Test
    void revokeAdmin_userNotFound_throwsResourceNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.revokeAdmin(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── enable / disable ─────────────────────────────────────────────────────

    @Test
    void setEnabled_false_disablesUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UserResponse resp = userService.setEnabled(1L, false);

        assertThat(resp.enabled()).isFalse();
    }

    @Test
    void setEnabled_true_enablesUser() {
        testUser.setEnabled(false);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        UserResponse resp = userService.setEnabled(1L, true);

        assertThat(resp.enabled()).isTrue();
    }

    @Test
    void setEnabled_userNotFound_throwsResourceNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setEnabled(99L, false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── updates ──────────────────────────────────────────────────────────────

    @Test
    void updateUser_validRequest_updatesAndReturnsUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        clm.user.demo.dto.requests.UpdateUserRequest req = new clm.user.demo.dto.requests.UpdateUserRequest("new@test.com", "New Name", null);
        UserResponse resp = userService.updateUser(1L, req);

        assertThat(resp.email()).isEqualTo("new@test.com");
        assertThat(resp.name()).isEqualTo("New Name");
    }

    @Test
    void updateUser_duplicateEmail_throwsDuplicateEmailException() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByEmail("dup@test.com")).willReturn(true);

        clm.user.demo.dto.requests.UpdateUserRequest req = new clm.user.demo.dto.requests.UpdateUserRequest("dup@test.com", "New Name", null);

        assertThatThrownBy(() -> userService.updateUser(1L, req))
                .isInstanceOf(clm.user.demo.exceptions.exceptions.DuplicateEmailException.class);
    }

    @Test
    void updateUser_userNotFound_throwsResourceNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());
        clm.user.demo.dto.requests.UpdateUserRequest req = new clm.user.demo.dto.requests.UpdateUserRequest("new@test.com", "New Name", null);

        assertThatThrownBy(() -> userService.updateUser(99L, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── reset password ───────────────────────────────────────────────────────

    @Test
    void resetPassword_updatesPassword() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.encode("newPass123!")).willReturn("encodedPass");
        given(userRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        clm.user.demo.dto.requests.ResetPasswordRequest req = new clm.user.demo.dto.requests.ResetPasswordRequest("newPass123!");
        UserResponse resp = userService.resetPassword(1L, req);

        assertThat(testUser.getPassword()).isEqualTo("encodedPass");
        assertThat(resp.email()).isEqualTo("user@test.com");
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void deleteUser_exists_deletesUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        org.mockito.Mockito.verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_notFound_throwsResourceNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private static Role role(int id, String name) {
        var r = new Role();
        r.setId(id);
        r.setName(name);
        return r;
    }
}