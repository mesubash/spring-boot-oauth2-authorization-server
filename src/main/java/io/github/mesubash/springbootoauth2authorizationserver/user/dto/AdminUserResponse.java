package io.github.mesubash.springbootoauth2authorizationserver.user.dto;

import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(

        UUID id,
        String username,
        String email,

        boolean enabled,
        boolean accountNonExpired,
        boolean accountNonLocked,
        boolean credentialsNonExpired,

        Set<String> roles

) {
}