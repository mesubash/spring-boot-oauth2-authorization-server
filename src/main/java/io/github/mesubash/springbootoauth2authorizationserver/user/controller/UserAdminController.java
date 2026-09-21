package io.github.mesubash.springbootoauth2authorizationserver.user.controller;

import io.github.mesubash.springbootoauth2authorizationserver.user.dto.AdminUserResponse;
import io.github.mesubash.springbootoauth2authorizationserver.user.dto.UpdateUserStatusRequest;
import io.github.mesubash.springbootoauth2authorizationserver.user.service.UserAdminService;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(
            UserAdminService userAdminService
    ) {
        this.userAdminService =
                userAdminService;
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
}