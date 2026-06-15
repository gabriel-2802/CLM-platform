package clm.demo.repositories;

import clm.demo.models.Contract;
import clm.demo.models.ContractDetails;
import clm.demo.models.enums.ContractStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    @Query("""
        SELECT c.id FROM Contract c
        WHERE c.contractStatus = :active
        AND c.endDate < :today
        ORDER BY c.id ASC
    """)
    List<Long> findExpiredContractIds(@Param("today") LocalDate today,
                                      @Param("active") ContractStatus active,
                                      Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Contract c
        SET c.contractStatus = :archived
        WHERE c.id IN :ids
    """)
    void archiveContractsByIds(@Param("ids") List<Long> ids,
                               @Param("archived") ContractStatus archived);

    @Query("""
        SELECT c FROM Contract c
        WHERE c.contractStatus = :status
        AND c.endDate BETWEEN :from AND :to
        ORDER BY c.endDate ASC
    """)
    List<Contract> findExpiringContracts(@Param("status") ContractStatus status,
                                         @Param("from")   LocalDate from,
                                         @Param("to")     LocalDate to);
     @Query("""
         SELECT c.id FROM Contract c
         WHERE c.contractStatus = :terminationDue
         AND c.terminationDate = :today
         ORDER BY c.id ASC
     """)
     List<Long> findTerminationDueContractIds(@Param("today") LocalDate today,
                                              @Param("terminationDue") ContractStatus terminationDue,
                                              Pageable pageable);

     @Modifying
     @Transactional
     @Query("""
         UPDATE Contract c
         SET c.contractStatus = :terminated
         WHERE c.id IN :ids
     """)
     void terminateContractsByIds(@Param("ids") List<Long> ids,
                                  @Param("terminated") ContractStatus terminated);

     @Modifying
     @Transactional
     @Query("""
         UPDATE Contract c
         SET c.contractStatus = :terminated
         WHERE c.contractStatus = :terminationDue
         AND c.terminationDate = :today
     """)
     int processTerminationDueContracts(@Param("terminated")      ContractStatus terminated,
                                        @Param("terminationDue")  ContractStatus terminationDue,
                                        @Param("today")           LocalDate today);

    /**
     * Finds ACTIVE contracts that have had no renegotiation (new ContractDetails with
     * a backing appendix) within the look-back window defined by {@code cutoffDate}.
     */
    @Query("""
        SELECT c FROM Contract c
        WHERE c.contractStatus = :status
        AND c.generatedAt < :cutoffDate
        AND NOT EXISTS (
            SELECT cd FROM ContractDetails cd
            WHERE cd.contract = c
            AND cd.appendix IS NOT NULL
            AND cd.createdAt >= :cutoffDate
        )
        ORDER BY c.clientId ASC
    """)
    List<Contract> findInactiveClientContracts(@Param("status")     ContractStatus status,
                                               @Param("cutoffDate") LocalDate cutoffDate);
}
