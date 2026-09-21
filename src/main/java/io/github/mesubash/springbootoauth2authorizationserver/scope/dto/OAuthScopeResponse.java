package io.github.mesubash.springbootoauth2authorizationserver.scope.dto;

import java.util.UUID;

public record OAuthScopeResponse(
        UUID id,
        String name,
        String description,
        boolean enabled
) {
}