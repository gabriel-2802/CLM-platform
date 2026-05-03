package clm.client.demo.repositories;

import clm.client.demo.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t JOIN FETCH t.client WHERE t.userId = :userId ORDER BY t.date DESC")
    List<Task> findAllByUserIdOrderByDateDesc(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t JOIN FETCH t.client ORDER BY t.date DESC")
    List<Task> findAllWithClientOrderByDateDesc();

    @Query("SELECT t FROM Task t JOIN FETCH t.client WHERE t.client.id = :clientId ORDER BY t.date ASC")
    List<Task> findAllByClientIdOrderByDateAsc(@Param("clientId") Long clientId);
}