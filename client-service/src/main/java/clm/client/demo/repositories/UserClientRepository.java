package clm.client.demo.repositories;

import clm.client.demo.models.UserClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserClientRepository extends JpaRepository<UserClient, Long> {
    boolean existsByClientIdAndUserId(Long clientId, Long userId);
    List<UserClient> findAllByClientId(Long clientId);
    List<UserClient> findAllByUserId(Long userId);
    void deleteByClientIdAndUserId(Long clientId, Long userId);
    void deleteByClientId(Long clientId);
}