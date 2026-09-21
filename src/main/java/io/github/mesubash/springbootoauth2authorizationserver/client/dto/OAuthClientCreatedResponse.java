package io.github.mesubash.springbootoauth2authorizationserver.client.dto;

public record OAuthClientCreatedResponse(

        OAuthClientResponse client,
        String clientSecret
) {
}