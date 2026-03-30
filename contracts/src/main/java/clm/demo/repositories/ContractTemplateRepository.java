package clm.demo.repositories;

import clm.demo.models.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ContractTemplate entity.
 * Handles CRUD operations and custom queries for contract templates.
 */
@Repository
public interface ContractTemplateRepository extends JpaRepository<Template, Long> {

    /**
     * Efficiently updates the isFullyMapped status without loading the full entity.
     * Uses a direct UPDATE query at the database level for optimal performance.
     *
     * @param templateId the template ID
     * @param isFullyMapped the new fully mapped status
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Template c SET c.isFullyMapped = :isFullyMapped WHERE c.id = :templateId")
    void updateFullyMappedStatus(@Param("templateId") Long templateId, @Param("isFullyMapped") Boolean isFullyMapped);

    /**
     * Bulk updates the fully mapped status based on a condition.
     * Sets isFullyMapped = true only when all fields in the template have a label.
     *
     * @param templateId the template ID
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        UPDATE Template c 
        SET c.isFullyMapped = (
            SELECT CASE 
                WHEN COUNT(f) = 0 THEN true
                WHEN COUNT(CASE WHEN f.fieldLabel IS NULL THEN 1 END) = 0 THEN true
                ELSE false
            END
            FROM TemplateField f
            WHERE f.contractTemplate.id = c.id
        )
        WHERE c.id = :templateId
    """)
    void updateFullyMappedStatusBasedOnFields(@Param("templateId") Long templateId);
}

