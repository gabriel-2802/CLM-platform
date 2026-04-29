package clm.client.demo.repositories;

import clm.client.demo.models.ClientHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientHistoryRepository extends JpaRepository<ClientHistory, Long> {
    List<ClientHistory> findAllByClientId(Long clientId);
    Optional<ClientHistory> findByClientIdAndYear(Long clientId, int year);
}