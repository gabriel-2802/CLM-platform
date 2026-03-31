package clm.demo.repositories;

import clm.demo.models.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ContractTemplate entity.
 * Handles CRUD operations and custom queries for contract templates.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
}

