package clm.demo.repositories;

import clm.demo.models.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ContractTemplate entity.
 * Handles CRUD operations and custom queries for contract templates.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    /**
     * Finds a template by its name.
     *
     * @param templateName the template name
     * @return Optional containing the template if found
     */
    Optional<Template> findByTemplateName(String templateName);
}

