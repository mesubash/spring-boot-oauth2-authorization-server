package io.github.mesubash.springbootoauth2authorizationserver.security;

import io.github.mesubash.springbootoauth2authorizationserver.audit.service.SecurityAuditService;
import io.github.mesubash.springbootoauth2authorizationserver.user.service.LoginAttemptService;
import org.springframework.context.event.EventListener;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;
    private final SecurityAuditService auditService;

    public AuthenticationEventListener(
            LoginAttemptService loginAttemptService,
            SecurityAuditService auditService
    ) {
        this.loginAttemptService =
                loginAttemptService;
        this.auditService =
                auditService;
    }


    @EventListener
    public void onAuthenticationFailure(
            AuthenticationFailureBadCredentialsEvent event
    ) {

        if (!(event.getAuthentication()
                instanceof UsernamePasswordAuthenticationToken)) {
            return;
        }

        loginAttemptService.recordFailedLogin(
                event.getAuthentication().getName()
        );
        auditService.record(
                "LOGIN_FAILURE",
                event.getAuthentication().getName(),
                "USER",
                null,
                "FAILURE",
                "Invalid credentials"
        );
    }


    @EventListener
    public void onAuthenticationSuccess(
            AuthenticationSuccessEvent event
    ) {

        if (!(event.getAuthentication()
                instanceof UsernamePasswordAuthenticationToken)) {
            return;
        }

        loginAttemptService.recordSuccessfulLogin(
                event.getAuthentication().getName()
        );
        auditService.record(
                "LOGIN_SUCCESS",
                event.getAuthentication().getName(),
                "USER",
                event.getAuthentication().getName(),
                "SUCCESS",
                null
        );
    }
}