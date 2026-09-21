package io.github.mesubash.springbootoauth2authorizationserver.client.dto;

public record OAuthClientSecretResponse(
        String clientId,
        String clientSecret
) {
}
