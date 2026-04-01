package clm.demo.repositories;

import clm.demo.models.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for GeneratedContract entity.
 * Handles CRUD operations and custom queries for generated contracts.
 * Supports dynamic filtering via JPA Specifications for efficient searching.
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    /**
     * Finds contracts that contain specific field values.
     * Used for filtering by labelValues in the SearchRequest.
     *
     * @param templateFieldLabels the field labels to search for
     * @param fieldValue         the field value to match
     * @return list of contracts matching the criteria
     */
    @Query("""
        SELECT DISTINCT c FROM Contract c
        JOIN c.fieldValues fv
        JOIN fv.templateField tf
        WHERE tf.fieldLabel IN :labels
          AND LOWER(fv.fieldValue) LIKE LOWER(CONCAT('%', :value, '%'))
        """)
    List<Contract> findByFieldValues(
            @Param("labels") List<String> templateFieldLabels,
            @Param("value") String fieldValue
    );

    /**
     * Finds contracts matching date range (created between dates).
     *
     * @param createdAfter  the start date (inclusive)
     * @param createdBefore the end date (inclusive)
     * @return list of contracts created within the date range
     */
    @Query("""
        SELECT c FROM Contract c
        WHERE CAST(c.createdAt AS date) >= :createdAfter
          AND CAST(c.createdAt AS date) <= :createdBefore
        """)
    List<Contract> findByCreatedDateRange(
            @Param("createdAfter") LocalDate createdAfter,
            @Param("createdBefore") LocalDate createdBefore
    );

}

