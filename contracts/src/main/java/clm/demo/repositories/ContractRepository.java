package clm.demo.repositories;

import clm.demo.models.Contract;
import clm.demo.models.enums.ContractStatus;
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
                                @Param("today")    LocalDate today);

    @Query("""
        SELECT c FROM Contract c
        WHERE c.contractStatus = :status
        AND c.contractEndDate BETWEEN :from AND :to
        ORDER BY c.contractEndDate ASC
    """)
    List<Contract> findExpiringContracts(@Param("status") ContractStatus status,
                                         @Param("from")   LocalDate from,
                                         @Param("to")     LocalDate to);

    /**
     * Finds ACTIVE contracts for clients who have not had a rate change within the look-back window.
     *
     * A client qualifies when every contract they hold within the last {@code months} months
     * carries the same {@code contractValue} as their current ACTIVE contract — i.e. no
     * renegotiation of the monetary terms has occurred in that period.
     *
     * NULL values are treated as equal to each other and different from any non-NULL value.
     */
    @Query("""
        SELECT c FROM Contract c
        WHERE c.contractStatus = :status
        AND NOT EXISTS (
            SELECT c2 FROM Contract c2
            WHERE c2.clientId = c.clientId
            AND c2.id <> c.id
            AND c2.createdAt >= :cutoffDate
            AND NOT (
                (c2.contractValue IS NOT NULL AND c.contractValue IS NOT NULL
                    AND c2.contractValue = c.contractValue)
                OR (c2.contractValue IS NULL AND c.contractValue IS NULL)
            )
        )
        ORDER BY c.clientId ASC
    """)
    List<Contract> findInactiveClientContracts(@Param("status")     ContractStatus status,
                                               @Param("cutoffDate") LocalDateTime cutoffDate);
}
