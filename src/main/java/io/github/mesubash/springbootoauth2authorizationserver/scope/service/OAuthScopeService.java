package io.github.mesubash.springbootoauth2authorizationserver.scope.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.mesubash.springbootoauth2authorizationserver.common.exception.InvalidRequestException;
import io.github.mesubash.springbootoauth2authorizationserver.scope.entity.OAuthScopeEntity;
import io.github.mesubash.springbootoauth2authorizationserver.scope.repository.OAuthScopeRepository;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

import org.springframework.stereotype.Service;

@Service
public class OAuthScopeService {

    private static final Set<String> STANDARD_OIDC_SCOPES =
            Set.of(
                    OidcScopes.OPENID,
                    OidcScopes.PROFILE,
                    OidcScopes.EMAIL,
                    OidcScopes.ADDRESS,
                    OidcScopes.PHONE
            );

    private final OAuthScopeRepository scopeRepository;


    public OAuthScopeService(
            OAuthScopeRepository scopeRepository
    ) {
        this.scopeRepository = scopeRepository;
    }

    public Set<String> validateAndNormalize(
            Set<String> requestedScopes
    ) {

        if (requestedScopes == null
                || requestedScopes.isEmpty()) {

            throw new InvalidRequestException(
                    "At least one scope is required"
            );
        }

        Set<String> normalizedScopes =
                requestedScopes
                        .stream()
                        .map(String::trim)
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        Set<String> customScopes =
                normalizedScopes
                        .stream()
                        .filter(scope ->
                                !STANDARD_OIDC_SCOPES
                                        .contains(scope)
                        )
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        if (customScopes.isEmpty()) {
            return normalizedScopes;
        }

        Set<String> registeredScopes =
                scopeRepository
                        .findAllByNameInAndEnabledTrue(
                                customScopes
                        )
                        .stream()
                        .map(OAuthScopeEntity::getName)
                        .collect(Collectors.toSet());

        Set<String> invalidScopes =
                new LinkedHashSet<>(customScopes);

        invalidScopes.removeAll(
                registeredScopes
        );

        if (!invalidScopes.isEmpty()) {

            throw new InvalidRequestException(
                    "Unknown or disabled OAuth scopes: "
                            + String.join(", ", invalidScopes)
            );
        }

        return normalizedScopes;
    }
}
