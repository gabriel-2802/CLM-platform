package clm.user.demo.config;

import clm.user.demo.models.RoleName;
import clm.user.demo.models.User;
import clm.user.demo.repositories.RoleRepository;
import clm.user.demo.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public AdminSeeder(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.email:admin@example.com}") String adminEmail,
                       @Value("${app.admin.password:Admin123!}") String adminPassword,
                       @Value("${app.admin.name:Admin}") String adminName) {
        this.userRepository  = userRepository;
        this.roleRepository  = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail      = adminEmail;
        this.adminPassword   = adminPassword;
        this.adminName       = adminName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        var userRole  = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER missing — check migrations"));
        var adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN missing — check migrations"));

        var admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setName(adminName);
        admin.getRoles().add(userRole);
        admin.getRoles().add(adminRole);

        userRepository.save(admin);
        log.info("Seeded default admin: {}", adminEmail);
    }
}
