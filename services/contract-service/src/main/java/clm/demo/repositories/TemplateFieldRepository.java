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
}


