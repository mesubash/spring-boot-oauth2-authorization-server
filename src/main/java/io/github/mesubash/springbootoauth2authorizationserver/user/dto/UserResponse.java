package io.github.mesubash.springbootoauth2authorizationserver.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email
) {
}