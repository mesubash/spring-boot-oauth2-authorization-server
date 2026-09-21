package io.github.mesubash.springbootoauth2authorizationserver.audit.controller;


import io.github.mesubash.springbootoauth2authorizationserver.audit.dto.SecurityAuditEventResponse;
import io.github.mesubash.springbootoauth2authorizationserver.audit.service.SecurityAuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-events")
public class SecurityAuditController {

    private final SecurityAuditService auditService;

    public SecurityAuditController(
            SecurityAuditService auditService
    ) {
        this.auditService = auditService;
    }

    @GetMapping
    public Page<SecurityAuditEventResponse> list(
            Pageable pageable
    ) {
        return auditService.list(pageable);
    }
}