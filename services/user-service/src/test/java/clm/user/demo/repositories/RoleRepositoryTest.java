package clm.user.demo.repositories;

import clm.user.demo.models.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.flywaydb.core.Flyway flyway;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private clm.user.demo.config.AdminSeeder adminSeeder;

    @Test
    void testSaveAndFindByName() {
        Role role = new Role();
        role.setName("ROLE_TEST");
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByName("ROLE_TEST");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_TEST");
    }
}
