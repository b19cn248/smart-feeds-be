// feeds-dao/src/main/java/com/olh/feeds/dao/repository/UserRepository.java
package com.olh.feeds.dao.repository;

import com.olh.feeds.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u
        FROM User u
        JOIN TeamUser tu ON u.id = tu.userId
        WHERE tu.teamId = :teamId AND tu.isDeleted = false AND u.isDeleted = false
        """)
    List<User> findByTeamId(@Param("teamId") Long teamId);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.email LIKE %:query% OR u.name LIKE %:query%
        AND u.isDeleted = false
        """)
    List<User> findUsersByQuery(@Param("query") String query);
    Optional<User> findByUsername(String username);
    Optional<User> findByKeycloakId(String keycloakId);
    boolean existsByUsername(String username);
    boolean existsByKeycloakId(String keycloakId);
}