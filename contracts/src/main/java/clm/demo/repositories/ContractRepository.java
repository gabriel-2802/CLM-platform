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
}
