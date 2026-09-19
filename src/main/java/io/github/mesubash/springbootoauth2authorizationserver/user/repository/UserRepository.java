package io.github.mesubash.springbootoauth2authorizationserver.user.repository;

import java.util.Optional;
import java.util.UUID;

import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByUsername(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByUsernameOrEmail(
            String username,
            String email
    );

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
