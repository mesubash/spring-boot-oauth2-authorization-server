package io.github.mesubash.springbootoauth2authorizationserver.client.controller;

import io.github.mesubash.springbootoauth2authorizationserver.client.dto.*;
import io.github.mesubash.springbootoauth2authorizationserver.client.service.OAuthClientService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/clients")
public class OAuthClientController {

    private final OAuthClientService clientService;

    public OAuthClientController(
            OAuthClientService clientService
    ) {
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OAuthClientCreatedResponse create(
            @Valid
            @RequestBody
            CreateOAuthClientRequest request
    ) {

        return clientService.create(request);
    }

    @GetMapping("/{clientId}")
    public OAuthClientResponse getByClientId(
            @PathVariable
            String clientId
    ) {

        return clientService.getByClientId(clientId);
    }

    @GetMapping
    public List<OAuthClientResponse> list() {

        return clientService.list();
    }

    @PutMapping("/{clientId}")
    public OAuthClientResponse update(
            @PathVariable String clientId,
            @Valid @RequestBody
            UpdateOAuthClientRequest request
    ) {

        return clientService.update(
                clientId,
                request
        );
    }

    @PostMapping("/{clientId}/secret/rotate")
    public OAuthClientSecretResponse rotateSecret(
            @PathVariable String clientId
    ) {

        return clientService.rotateSecret(
                clientId
        );
    }

    @DeleteMapping("/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String clientId
    ) {

        clientService.delete(clientId);
    }
}