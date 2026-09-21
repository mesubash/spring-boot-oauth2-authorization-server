package io.github.mesubash.springbootoauth2authorizationserver.scope.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.mesubash.springbootoauth2authorizationserver.scope.entity.OAuthScopeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthScopeRepository
        extends JpaRepository<OAuthScopeEntity, UUID> {

    Optional<OAuthScopeEntity> findByName(String name);

    boolean existsByName(String name);

    List<OAuthScopeEntity> findAllByNameInAndEnabledTrue(
            Collection<String> names
    );
    List<OAuthScopeEntity> findAllByOrderByNameAsc();
}