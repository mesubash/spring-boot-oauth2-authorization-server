package io.github.mesubash.springbootoauth2authorizationserver.user.controller;

import io.github.mesubash.springbootoauth2authorizationserver.user.dto.AdminResetPasswordRequest;
import io.github.mesubash.springbootoauth2authorizationserver.user.dto.AdminUserResponse;
import io.github.mesubash.springbootoauth2authorizationserver.user.dto.UpdateUserStatusRequest;
import io.github.mesubash.springbootoauth2authorizationserver.user.service.PasswordService;
import io.github.mesubash.springbootoauth2authorizationserver.user.service.UserAdminService;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final PasswordService passwordService;

    public UserAdminController(
            UserAdminService userAdminService,
            PasswordService passwordService
    ) {
        this.userAdminService =
                userAdminService;
        this.passwordService =
                passwordService;
    }


    @GetMapping
    public Page<AdminUserResponse> list(
            Pageable pageable
    ) {
        return userAdminService.list(pageable);
    }


    @GetMapping("/{userId}")
    public AdminUserResponse get(
            @PathVariable UUID userId
    ) {
        return userAdminService.get(userId);
    }


    @PutMapping("/{userId}/status")
    public AdminUserResponse updateStatus(
            @PathVariable UUID userId,
            @Valid
            @RequestBody
            UpdateUserStatusRequest request
    ) {

        return userAdminService.updateStatus(
                userId,
                request
        );
    }


    @PutMapping("/{userId}/roles/{roleName}")
    public AdminUserResponse addRole(
            @PathVariable UUID userId,
            @PathVariable String roleName
    ) {

        return userAdminService.addRole(
                userId,
                roleName
        );
    }


    @DeleteMapping("/{userId}/roles/{roleName}")
    public AdminUserResponse removeRole(
            @PathVariable UUID userId,
            @PathVariable String roleName
    ) {

        return userAdminService.removeRole(
                userId,
                roleName
        );
    }

    @PutMapping("/{userId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(
            @PathVariable UUID userId,
            @Valid
            @RequestBody
            AdminResetPasswordRequest request
    ) {

        passwordService.resetPassword(
                userId,
                request.newPassword()
        );
    }
}