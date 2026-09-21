package io.github.mesubash.springbootoauth2authorizationserver.audit.service;

import io.github.mesubash.springbootoauth2authorizationserver.audit.dto.SecurityAuditEventResponse;
import io.github.mesubash.springbootoauth2authorizationserver.audit.entity.SecurityAuditEvent;
import io.github.mesubash.springbootoauth2authorizationserver.audit.repository.SecurityAuditEventRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class SecurityAuditService {

    private final SecurityAuditEventRepository repository;

    public SecurityAuditService(
            SecurityAuditEventRepository repository
    ) {
        this.repository = repository;
    }


    @Transactional
    public void record(
            String eventType,
            String targetType,
            String targetId,
            String outcome,
            String details
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String actor =
                authentication != null
                        && authentication.isAuthenticated()
                        ? authentication.getName()
                        : null;

        record(
                eventType,
                actor,
                targetType,
                targetId,
                outcome,
                details
        );
    }


    @Transactional
    public void record(
            String eventType,
            String actor,
            String targetType,
            String targetId,
            String outcome,
            String details
    ) {

        SecurityAuditEvent event =
                new SecurityAuditEvent();

        event.setEventType(eventType);
        event.setActor(actor);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setOutcome(outcome);
        event.setDetails(details);

        HttpServletRequest request =
                currentRequest();

        if (request != null) {

            event.setIpAddress(
                    request.getRemoteAddr()
            );

            event.setUserAgent(
                    request.getHeader("User-Agent")
            );
        }

        repository.save(event);
    }


    @Transactional(readOnly = true)
    public Page<SecurityAuditEventResponse> list(
            Pageable pageable
    ) {

        return repository
                .findAll(pageable)
                .map(this::toResponse);
    }


    private HttpServletRequest currentRequest() {

        if (RequestContextHolder
                .getRequestAttributes()
                instanceof ServletRequestAttributes attributes) {

            return attributes.getRequest();
        }

        return null;
    }


    private SecurityAuditEventResponse toResponse(
            SecurityAuditEvent event
    ) {

        return new SecurityAuditEventResponse(
                event.getId(),
                event.getEventType(),
                event.getActor(),
                event.getTargetType(),
                event.getTargetId(),
                event.getOutcome(),
                event.getIpAddress(),
                event.getDetails(),
                event.getCreatedAt()
        );
    }
}