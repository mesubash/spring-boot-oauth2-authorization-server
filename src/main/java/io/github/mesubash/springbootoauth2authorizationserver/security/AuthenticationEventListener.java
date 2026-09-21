package io.github.mesubash.springbootoauth2authorizationserver.security;

import io.github.mesubash.springbootoauth2authorizationserver.user.service.LoginAttemptService;
import org.springframework.context.event.EventListener;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;

    public AuthenticationEventListener(
            LoginAttemptService loginAttemptService
    ) {
        this.loginAttemptService =
                loginAttemptService;
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
    }
}