package clm.demo.repositories;

import clm.demo.models.TemplateField;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

/**
 * Repository for TemplateField entity.
 * Handles CRUD operations and custom queries for template fields (placeholders).
 */
@Repository
public interface TemplateFieldRepository extends JpaRepository<TemplateField, Long> {
}


