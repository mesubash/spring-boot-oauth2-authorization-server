package io.github.mesubash.springbootoauth2authorizationserver.scope.controller;

import io.github.mesubash.springbootoauth2authorizationserver.scope.dto.CreateOAuthScopeRequest;
import io.github.mesubash.springbootoauth2authorizationserver.scope.dto.OAuthScopeResponse;
import io.github.mesubash.springbootoauth2authorizationserver.scope.dto.UpdateOAuthScopeRequest;
import io.github.mesubash.springbootoauth2authorizationserver.scope.service.OAuthScopeService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scopes")
public class OAuthScopeController {

    private final OAuthScopeService scopeService;

    public OAuthScopeController(
            OAuthScopeService scopeService
    ) {
        this.scopeService = scopeService;
    }

    @GetMapping
    public List<OAuthScopeResponse> list() {
        return scopeService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OAuthScopeResponse create(
            @Valid
            @RequestBody
            CreateOAuthScopeRequest request
    ) {
        return scopeService.create(request);
    }

    @PutMapping("/{name}")
    public OAuthScopeResponse update(
            @PathVariable String name,
            @Valid
            @RequestBody
            UpdateOAuthScopeRequest request
    ) {
        return scopeService.update(
                name,
                request
        );
    }
}