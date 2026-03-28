package clm.demo.repositories;

import clm.demo.models.ContractFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository for ContractFieldValue entity.
 * Handles CRUD operations and custom queries for contract field values (audit trail).
 */
@Repository
public interface ContractFieldValueRepository extends JpaRepository<ContractFieldValue, Long> {

}

