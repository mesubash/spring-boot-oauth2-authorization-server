package io.github.mesubash.springbootoauth2authorizationserver.user.repository;


import java.util.Optional;
import java.util.UUID;

import io.github.mesubash.springbootoauth2authorizationserver.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByName(String name);
}
