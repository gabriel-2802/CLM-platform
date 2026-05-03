package clm.client.demo.repositories;

import clm.client.demo.models.ClientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientHistoryRepository extends JpaRepository<ClientHistory, Long> {

    @Query("SELECT h FROM ClientHistory h JOIN FETCH h.client WHERE h.client.id = :clientId")
    List<ClientHistory> findAllByClientId(@Param("clientId") Long clientId);

    @Query("SELECT h FROM ClientHistory h JOIN FETCH h.client WHERE h.client.id = :clientId AND h.year = :year")
    Optional<ClientHistory> findByClientIdAndYear(@Param("clientId") Long clientId, @Param("year") int year);
}