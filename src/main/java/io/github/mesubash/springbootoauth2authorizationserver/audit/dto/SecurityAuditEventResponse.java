package io.github.mesubash.springbootoauth2authorizationserver.audit.dto;
import java.time.Instant;
import java.util.UUID;

public record SecurityAuditEventResponse(

        UUID id,
        String eventType,
        String actor,
        String targetType,
        String targetId,
        String outcome,
        String ipAddress,
        String details,
        Instant createdAt

) {
}
