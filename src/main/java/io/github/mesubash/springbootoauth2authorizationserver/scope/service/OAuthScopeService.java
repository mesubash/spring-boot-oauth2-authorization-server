package io.github.mesubash.springbootoauth2authorizationserver.scope.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.mesubash.springbootoauth2authorizationserver.common.exception.InvalidRequestException;
import io.github.mesubash.springbootoauth2authorizationserver.common.exception.ResourceConflictException;
import io.github.mesubash.springbootoauth2authorizationserver.common.exception.ResourceNotFoundException;
import io.github.mesubash.springbootoauth2authorizationserver.scope.dto.CreateOAuthScopeRequest;
import io.github.mesubash.springbootoauth2authorizationserver.scope.dto.OAuthScopeResponse;
import io.github.mesubash.springbootoauth2authorizationserver.scope.dto.UpdateOAuthScopeRequest;
import io.github.mesubash.springbootoauth2authorizationserver.scope.entity.OAuthScopeEntity;
import io.github.mesubash.springbootoauth2authorizationserver.scope.repository.OAuthScopeRepository;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public OAuthScopeResponse create(
            CreateOAuthScopeRequest request
    ) {

        String name = request.name().trim();

        if (STANDARD_OIDC_SCOPES.contains(name)) {
            throw new ResourceConflictException(
                    "Scope is reserved by OpenID Connect"
            );
        }

        if (scopeRepository.existsByName(name)) {
            throw new ResourceConflictException(
                    "OAuth scope already exists"
            );
        }

        OAuthScopeEntity scope =
                new OAuthScopeEntity();

        scope.setName(name);

        scope.setDescription(
                request.description() == null
                        ? null
                        : request.description().trim()
        );

        scope.setEnabled(true);

        OAuthScopeEntity saved =
                scopeRepository.save(scope);

        return toResponse(saved);
    }

    public List<OAuthScopeResponse> list() {

        return scopeRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OAuthScopeResponse update(
            String name,
            UpdateOAuthScopeRequest request
    ) {

        OAuthScopeEntity scope =
                scopeRepository
                        .findByName(name)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "OAuth scope not found"
                                )
                        );

        scope.setDescription(
                request.description() == null
                        ? null
                        : request.description().trim()
        );

        scope.setEnabled(
                request.enabled()
        );

        OAuthScopeEntity saved =
                scopeRepository.save(scope);

        return toResponse(saved);
    }

    private OAuthScopeResponse toResponse(
            OAuthScopeEntity scope
    ) {

        return new OAuthScopeResponse(
                scope.getId(),
                scope.getName(),
                scope.getDescription(),
                scope.isEnabled()
        );
    }
}
