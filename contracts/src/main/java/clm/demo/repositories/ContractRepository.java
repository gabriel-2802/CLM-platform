package clm.demo.repositories;

import clm.demo.models.Contract;
import clm.demo.models.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for the {@link Contract} entity.
 *
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE Contract c
        SET c.contractStatus = :archived
        WHERE c.contractStatus = :active
        AND c.contractEndDate < :today
    """)
    int archiveExpiredContracts(@Param("archived") ContractStatus archived,
                                @Param("active")   ContractStatus active,
                                @Param("today") LocalDate today);

    /**
     * Returns all ACTIVE contracts whose end date falls within [today, deadline].
     * Used by the notifications service to warn about contracts expiring soon.
     */
    @Query("""
        SELECT c FROM Contract c
        WHERE c.contractStatus = :active
        AND c.contractEndDate BETWEEN :today AND :deadline
        ORDER BY c.contractEndDate ASC
    """)
    List<Contract> findExpiringContracts(@Param("active") ContractStatus active,
                                         @Param("today") LocalDate today,
                                         @Param("deadline") LocalDate deadline);

    /**
     * Returns the most recent contract per client, but only for clients whose
     * last contract was created before {@code cutoff}.
     * Used to detect clients that haven't been renegotiated recently.
     */
    @Query("""
        SELECT c FROM Contract c
        WHERE c.createdAt = (
            SELECT MAX(c2.createdAt) FROM Contract c2 WHERE c2.clientId = c.clientId
        )
        AND c.createdAt < :cutoff
        ORDER BY c.createdAt ASC
    """)
    List<Contract> findClientsNotRenegotiatedSince(@Param("cutoff") LocalDateTime cutoff);
}
