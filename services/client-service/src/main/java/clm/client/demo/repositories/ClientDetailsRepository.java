package clm.client.demo.repositories;

import clm.client.demo.models.ClientDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientDetailsRepository extends JpaRepository<ClientDetails, Long> {
    Optional<ClientDetails> findByClientId(Long clientId);
}