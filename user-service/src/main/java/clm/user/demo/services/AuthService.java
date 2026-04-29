package clm.user.demo.services;

import clm.user.demo.dto.requests.LoginRequest;
import clm.user.demo.dto.requests.RegisterRequest;
import clm.user.demo.dto.responses.AuthResponse;
import clm.user.demo.dto.responses.UserResponse;
import clm.user.demo.exceptions.DuplicateEmailException;
import clm.user.demo.exceptions.InvalidCredentialsException;
import clm.user.demo.models.RoleName;
import clm.user.demo.models.User;
import clm.user.demo.repositories.RoleRepository;
import clm.user.demo.repositories.UserRepository;
import clm.user.demo.security.JwtTokenProvider;
import clm.user.demo.security.UserDetailsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final String adminRegisterCode;
    private final long jwtExpirationMs;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       @Value("${app.admin.register-code:devcode123}") String adminRegisterCode,
                       @Value("${jwt.expiration:2592000000}") long jwtExpirationMs) {
        this.userRepository       = userRepository;
        this.roleRepository       = roleRepository;
        this.passwordEncoder      = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider        = tokenProvider;
        this.adminRegisterCode    = adminRegisterCode;
        this.jwtExpirationMs      = jwtExpirationMs;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        var user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());

        var userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found — check DB migrations"));
        user.getRoles().add(userRole);

        if (StringUtils.hasText(request.adminCode())
                && isAdminCode(request.adminCode())) {
            roleRepository.findByName(RoleName.ADMIN).ifPresent(user.getRoles()::add);
            log.info("Granted ROLE_ADMIN to '{}'", request.email());
        }

        userRepository.save(user);
        log.info("Registered new user '{}'", request.email());

        UserDetails userDetails = UserDetailsServiceImpl.toUserDetails(user);
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

    private boolean isAdminCode(String provided) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                adminRegisterCode.getBytes(StandardCharsets.UTF_8)
        );
    }
}
