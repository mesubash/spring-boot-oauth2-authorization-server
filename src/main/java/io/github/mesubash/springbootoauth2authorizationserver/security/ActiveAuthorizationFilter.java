package io.github.mesubash.springbootoauth2authorizationserver.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;

import org.springframework.web.filter.OncePerRequestFilter;

public class ActiveAuthorizationFilter
        extends OncePerRequestFilter {

    private final OAuth2AuthorizationService authorizationService;

    private final BearerTokenAuthenticationEntryPoint authenticationEntryPoint =
            new BearerTokenAuthenticationEntryPoint();

    public ActiveAuthorizationFilter(
            OAuth2AuthorizationService authorizationService
    ) {
        this.authorizationService =
                authorizationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        /*
         * If this is not a real Bearer request,
         * allow the rest of Spring Security to handle it.
         *
         * This also keeps our MockMvc jwt() tests working.
         */
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String tokenValue =
                jwtAuthentication
                        .getToken()
                        .getTokenValue();

        OAuth2Authorization authorization =
                authorizationService.findByToken(
                        tokenValue,
                        OAuth2TokenType.ACCESS_TOKEN
                );

        boolean active =
                authorization != null
                        && authorization.getAccessToken() != null
                        && authorization
                        .getAccessToken()
                        .isActive();

        if (!active) {

            SecurityContextHolder
                    .clearContext();

            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(
                            "Access token is no longer active"
                    )
            );

            return;
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}