package io.github.mesubash.springbootoauth2authorizationserver.scope.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOAuthScopeRequest(

        @Size(max = 255)
        String description,

        @NotNull
        Boolean enabled

) {
}