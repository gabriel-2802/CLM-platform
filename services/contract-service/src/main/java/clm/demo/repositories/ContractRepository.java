package clm.demo.repositories;

import clm.demo.models.Contract;
import clm.demo.models.ContractDetails;
import clm.demo.models.enums.ContractStatus;
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

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE clm.contract
        SET contract_status = 'ARCHIVED'
        WHERE document_id IN (
            SELECT c.document_id FROM clm.contract c
            WHERE c.contract_status = 'ACTIVE'
            AND c.auto_renew = false
            AND (
                SELECT MAX(cd.end_date)
                FROM clm.contract_details cd
                WHERE cd.contract_id = c.document_id
            ) < :today
        )
    """, nativeQuery = true)
    int archiveExpiredContracts(@Param("today") LocalDate today);

    @Query("""
        SELECT DISTINCT c FROM Contract c
        JOIN c.contractDetailsList cd
        WHERE c.contractStatus = :status
        AND cd.endDate BETWEEN :from AND :to
        ORDER BY cd.endDate ASC
    """)
    List<Contract> findExpiringContracts(@Param("status") ContractStatus status,
                                         @Param("from")   LocalDate from,
                                         @Param("to")     LocalDate to);

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
