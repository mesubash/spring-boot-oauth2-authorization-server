package io.github.mesubash.springbootoauth2authorizationserver.client.dto;


import io.github.mesubash.springbootoauth2authorizationserver.client.model.OAuthClientType;

import java.util.Set;

public record OAuthClientResponse(

        String clientId,
        String clientName,
        OAuthClientType clientType,

        Set<String> redirectUris,
        Set<String> postLogoutRedirectUris,
        Set<String> scopes,
        Set<String> authorizationGrantTypes,

        boolean requireProofKey,
        boolean requireAuthorizationConsent
) {
}