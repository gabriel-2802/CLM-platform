package clm.client.demo.repositories;

import clm.client.demo.models.WorkPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkPointRepository extends JpaRepository<WorkPoint, Long> {

    @Query("SELECT w FROM WorkPoint w JOIN FETCH w.client WHERE w.client.id = :clientId")
    List<WorkPoint> findAllByClientId(@Param("clientId") Long clientId);

    @Query("SELECT w FROM WorkPoint w JOIN FETCH w.client WHERE w.id = :id AND w.client.id = :clientId")
    Optional<WorkPoint> findByIdAndClientId(@Param("id") Long id, @Param("clientId") Long clientId);
}