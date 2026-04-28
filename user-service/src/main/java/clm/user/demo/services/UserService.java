package clm.user.demo.services;

import clm.user.demo.dto.requests.ResetPasswordRequest;
import clm.user.demo.dto.requests.UpdateUserRequest;
import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.exceptions.DuplicateEmailException;
import clm.user.demo.exceptions.ResourceNotFoundException;
import clm.user.demo.models.User;
import clm.user.demo.repositories.RoleRepository;
import clm.user.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findOrThrow(id);

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        user.setEmail(request.email());
        user.setName(request.name());

        if (request.role() != null) {
            var userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER missing"));
            user.getRoles().clear();
            user.getRoles().add(userRole);

            switch (request.role()) {
                case "ADMIN" -> roleRepository.findByName("ROLE_ADMIN").ifPresent(user.getRoles()::add);
                case "MANAGER" -> roleRepository.findByName("ROLE_MANAGER").ifPresent(user.getRoles()::add);
            }
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse resetPassword(Long id, ResetPasswordRequest request) {
        User user = findOrThrow(id);
        user.setPassword(passwordEncoder.encode(request.password()));
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserResponse grantAdmin(Long id) {
        User user = findOrThrow(id);
        var adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN missing"));
        user.getRoles().add(adminRole);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse revokeAdmin(Long id) {
        User user = findOrThrow(id);
        user.getRoles().removeIf(r -> r.getName().equals("ROLE_ADMIN"));
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = findOrThrow(id);
        user.setEnabled(enabled);
        return UserResponse.from(userRepository.save(user));
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
