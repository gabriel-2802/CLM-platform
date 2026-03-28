package clm.demo.repositories;

import clm.demo.models.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for FieldMapping entity.
 * Handles CRUD operations and custom queries for field mappings.
 */
@Repository
public interface FieldMappingRepository extends JpaRepository<FieldMapping, Long> {

}

