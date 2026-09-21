package io.github.mesubash.springbootoauth2authorizationserver.user.controller;

import io.github.mesubash.springbootoauth2authorizationserver.user.dto.ChangePasswordRequest;
import io.github.mesubash.springbootoauth2authorizationserver.user.service.PasswordService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private final PasswordService passwordService;

    public AccountController(
            PasswordService passwordService
    ) {
        this.passwordService = passwordService;
    }


    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            Authentication authentication,
            @Valid
            @RequestBody
            ChangePasswordRequest request
    ) {

        passwordService.changePassword(
                authentication.getName(),
                request.currentPassword(),
                request.newPassword()
        );
    }
}