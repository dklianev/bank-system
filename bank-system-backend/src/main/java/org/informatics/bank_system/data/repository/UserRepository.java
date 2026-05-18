package org.informatics.bank_system.data.repository;

import org.informatics.bank_system.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.client WHERE u.username = :username")
    Optional<User> findByUsernameWithClient(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByClientId(Long clientId);
}
