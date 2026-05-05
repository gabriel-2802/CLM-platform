package clm.demo.repositories;

import clm.demo.models.DocumentFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentFieldValueRepository extends JpaRepository<DocumentFieldValue, Long> {
}
