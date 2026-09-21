package io.github.mesubash.springbootoauth2authorizationserver.client.dto;

import io.github.mesubash.springbootoauth2authorizationserver.client.model.OAuthClientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateOAuthClientRequest (

    @NotBlank
    @Size(max = 100)
    String clientName,

    @NotNull
    OAuthClientType clientType,

    @NotEmpty
    Set<@NotBlank String> redirectUris,

    Set<@NotBlank String> postLogoutRedirectUris,

    @NotEmpty
    Set<@NotBlank String> scopes,

    @NotNull
    Boolean requireAuthorizationConsent){


}
