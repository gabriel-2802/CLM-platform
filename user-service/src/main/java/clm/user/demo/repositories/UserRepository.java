package clm.user.demo.repositories;

import clm.user.demo.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    @Override
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = "roles")
    @Override
    List<User> findAll();

    boolean existsByEmail(String email);
}