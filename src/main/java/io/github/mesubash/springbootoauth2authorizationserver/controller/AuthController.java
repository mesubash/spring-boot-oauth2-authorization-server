package io.github.mesubash.springbootoauth2authorizationserver.controller;

import io.github.mesubash.springbootoauth2authorizationserver.user.dto.RegisterUserRequest;
import io.github.mesubash.springbootoauth2authorizationserver.user.dto.UserResponse;
import io.github.mesubash.springbootoauth2authorizationserver.user.service.UserRegistrationService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRegistrationService registrationService;

    public AuthController(
            UserRegistrationService registrationService
    ) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterUserRequest request
    ) {

        return registrationService.register(request);
    }
}