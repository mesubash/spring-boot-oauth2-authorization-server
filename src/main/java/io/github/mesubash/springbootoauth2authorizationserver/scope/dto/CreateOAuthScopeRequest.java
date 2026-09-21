package io.github.mesubash.springbootoauth2authorizationserver.scope.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOAuthScopeRequest(

        @NotBlank
        @Size(max = 100)
        @Pattern(
                regexp = "^[a-zA-Z0-9._:-]+$",
                message = "Scope contains invalid characters"
        )
        String name,

        @Size(max = 255)
        String description

) {
}
