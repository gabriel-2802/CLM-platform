package clm.client.demo.repositories;

import clm.client.demo.models.WorkPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkPointRepository extends JpaRepository<WorkPoint, Long> {
    List<WorkPoint> findAllByClientId(Long clientId);
    Optional<WorkPoint> findByIdAndClientId(Long id, Long clientId);
}