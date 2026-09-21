package io.github.mesubash.springbootoauth2authorizationserver.user.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

import org.springframework.stereotype.Service;

@Service
public class OidcUserClaimsService {

    private final UserRepository userRepository;

    public OidcUserClaimsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> loadClaims(
            String username,
            Set<String> authorizedScopes
    ) {

        UserEntity user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "OIDC user no longer exists"
                        )
                );

        Map<String, Object> claims =
                new LinkedHashMap<>();

        if (authorizedScopes.contains(
                OidcScopes.PROFILE
        )) {

            claims.put(
                    "preferred_username",
                    user.getUsername()
            );
        }

        if (authorizedScopes.contains(
                OidcScopes.EMAIL
        )) {

            claims.put(
                    "email",
                    user.getEmail()
            );
        }

        return claims;
    }
}