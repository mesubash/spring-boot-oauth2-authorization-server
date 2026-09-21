package io.github.mesubash.springbootoauth2authorizationserver.user.dto;


import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

        @NotNull
        Boolean enabled,

        @NotNull
        Boolean accountNonExpired,

        @NotNull
        Boolean accountNonLocked,

        @NotNull
        Boolean credentialsNonExpired

) {
}