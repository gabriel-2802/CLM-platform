package clm.user.demo.services;

import clm.user.demo.dto.requests.LoginRequest;
import clm.user.demo.dto.requests.RegisterRequest;
import clm.user.demo.dto.responses.AuthResponse;
import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.exceptions.DuplicateEmailException;
import clm.user.demo.exceptions.InvalidCredentialsException;
import clm.user.demo.models.Role;
import clm.user.demo.models.User;
import clm.user.demo.repositories.RoleRepository;
import clm.user.demo.repositories.UserRepository;
import clm.user.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.admin.register-code:devcode123}")
    private String adminRegisterCode;

    @Value("${jwt.expiration:2592000000}")
    private long jwtExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found — check DB migrations"));
        user.getRoles().add(userRole);

        if (StringUtils.hasText(request.adminCode())
                && request.adminCode().equals(adminRegisterCode)) {
            roleRepository.findByName("ROLE_ADMIN").ifPresent(user.getRoles()::add);
            log.info("Granted ROLE_ADMIN to '{}'", request.email());
        }

        userRepository.save(user);
        log.info("Registered new user '{}'", request.email());

        var authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toSet());
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .build();

        String token = tokenProvider.generateToken(userDetails);
        return AuthResponse.of(token, jwtExpirationMs, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserDetails userDetails;
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            userDetails = (UserDetails) authentication.getPrincipal();
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }
        
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        String token = tokenProvider.generateToken(userDetails);
        log.debug("Issued token for '{}'", request.email());
        return AuthResponse.of(token, jwtExpirationMs, UserResponse.from(user));
    }
}