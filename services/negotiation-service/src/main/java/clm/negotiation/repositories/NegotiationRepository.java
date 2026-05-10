package clm.negotiation.repositories;

import clm.negotiation.models.Negotiation;
import clm.negotiation.models.enums.NegotiationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    List<Negotiation> findByContractIdOrderByCreatedAtDesc(Long contractId);

    List<Negotiation> findByClientIdOrderByCreatedAtDesc(Integer clientId);

    @Query("""
        SELECT n FROM Negotiation n
        WHERE n.clientId = :clientId
          AND n.status   = :status
        ORDER BY n.createdAt DESC
        """)
    List<Negotiation> findByClientIdAndStatus(@Param("clientId") Integer clientId,
                                              @Param("status") NegotiationStatus status);

    @Query("""
        SELECT n FROM Negotiation n
        WHERE n.status     = 'ACCEPTED'
          AND n.resolvedAt < :cutoff
        ORDER BY n.resolvedAt DESC
        """)
    List<Negotiation> findAcceptedBefore(@Param("cutoff") LocalDateTime cutoff);
}
