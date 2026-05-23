package clm.user.demo.repositories;

import clm.user.demo.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.flywaydb.core.Flyway flyway;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private clm.user.demo.config.AdminSeeder adminSeeder;

    @Test
    void testSaveAndFindById() {
        User user = new User();
        user.setEmail("repo@test.com");
        user.setPassword("hashed");
        user.setName("Repo User");

        user = userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("repo@test.com");
    }

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setEmail("email@test.com");
        user.setPassword("hashed");
        user = userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("email@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void testFindAll() {
        User user1 = new User();
        user1.setEmail("u1@test.com");
        user1.setPassword("hashed");
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("u2@test.com");
        user2.setPassword("hashed");
        userRepository.save(user2);

        List<User> all = userRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void testFindAllPageable() {
        User user1 = new User();
        user1.setEmail("u1@test.com");
        user1.setPassword("hashed");
        userRepository.save(user1);

        Page<User> page = userRepository.findAll(PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void testExistsByEmail() {
        User user = new User();
        user.setEmail("exists@test.com");
        user.setPassword("hashed");
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("exists@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("nope@test.com")).isFalse();
    }
}
