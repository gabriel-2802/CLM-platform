package clm.client.demo.repositories;

import clm.client.demo.models.UserClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserClientRepository extends JpaRepository<UserClient, Long> {

    boolean existsByClientIdAndUserId(Long clientId, Long userId);

    @Query("SELECT uc FROM UserClient uc JOIN FETCH uc.client WHERE uc.client.id = :clientId")
    List<UserClient> findAllByClientId(@Param("clientId") Long clientId);

    @Query("SELECT uc FROM UserClient uc JOIN FETCH uc.client WHERE uc.userId = :userId")
    List<UserClient> findAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserClient uc WHERE uc.client.id = :clientId AND uc.userId = :userId")
    void deleteByClientIdAndUserId(@Param("clientId") Long clientId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserClient uc WHERE uc.client.id = :clientId")
    void deleteByClientId(@Param("clientId") Long clientId);
}