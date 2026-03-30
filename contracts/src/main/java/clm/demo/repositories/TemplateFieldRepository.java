package clm.demo.repositories;

import clm.demo.models.TemplateField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TemplateField entity.
 * Handles CRUD operations and custom queries for template fields (placeholders).
 */
@Repository
public interface TemplateFieldRepository extends JpaRepository<TemplateField, Long> {

    /**
     * Finds all template fields for a given template.
     */
    List<TemplateField> findByContractTemplateId(Long templateId);

    /**
     * Checks if all REQUIRED fields in a template have a field label set (i.e., are fully mapped).
     * Optional fields (isRequired = false) are ignored in this check.
     * This is an efficient count-based query that avoids loading full entities.
     *
     * @param templateId the template ID
     * @return true if all required fields have a non-null fieldLabel, false otherwise
     */
    @Query("""
        SELECT CASE 
            WHEN COUNT(*) = 0 THEN true
            WHEN COUNT(CASE WHEN f.fieldLabel IS NULL THEN 1 END) = 0 THEN true
            ELSE false
        END
        FROM TemplateField f
        WHERE f.contractTemplate.id = :templateId
        AND f.isRequired = true
    """)
    boolean areAllFieldsMapped(@Param("templateId") Long templateId);

    /**
     * Counts total fields without a label in a template.
     *
     * @param templateId the template ID
     * @return number of unmapped fields
     */
    @Query("SELECT COUNT(f) FROM TemplateField f WHERE f.contractTemplate.id = :templateId AND f.fieldLabel IS NULL")
    long countUnmappedFields(@Param("templateId") Long templateId);
}


