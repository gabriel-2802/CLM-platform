package clm.demo.repositories;

import clm.demo.models.GeneratedContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository for GeneratedContract entity.
 * Handles CRUD operations and custom queries for generated contracts.
 */
@Repository
public interface GeneratedContractRepository extends JpaRepository<GeneratedContract, Long> {

}

