package io.github.mesubash.springbootoauth2authorizationserver.audit.repository;

import java.util.UUID;

import io.github.mesubash.springbootoauth2authorizationserver.audit.entity.SecurityAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditEventRepository
        extends JpaRepository<SecurityAuditEvent, UUID> {
}