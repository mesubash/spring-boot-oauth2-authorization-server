package io.github.mesubash.springbootoauth2authorizationserver.client.service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import io.github.mesubash.springbootoauth2authorizationserver.client.dto.*;
import io.github.mesubash.springbootoauth2authorizationserver.client.model.OAuthClientType;
import io.github.mesubash.springbootoauth2authorizationserver.client.repository.OAuthClientManagementRepository;
import io.github.mesubash.springbootoauth2authorizationserver.common.exception.InvalidRequestException;
import io.github.mesubash.springbootoauth2authorizationserver.common.exception.ResourceNotFoundException;
import io.github.mesubash.springbootoauth2authorizationserver.scope.service.OAuthScopeService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OAuthClientService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenSettings tokenSettings;
    private final OAuthClientManagementRepository managementRepository;
    private final OAuthScopeService scopeService;

    public OAuthClientService(
            RegisteredClientRepository registeredClientRepository,
            PasswordEncoder passwordEncoder,
            TokenSettings tokenSettings,
            OAuthClientManagementRepository managementRepository,
            OAuthScopeService scopeService
    ) {
        this.registeredClientRepository =
                registeredClientRepository;

        this.passwordEncoder = passwordEncoder;
        this.tokenSettings = tokenSettings;
        this.managementRepository = managementRepository;
        this.scopeService = scopeService;
    }

    public OAuthClientCreatedResponse create(
            CreateOAuthClientRequest request
    ) {

        String clientId =
                UUID.randomUUID().toString();

        Set<String> scopes =
                scopeService.validateAndNormalize(
                        request.scopes()
                );

        String rawClientSecret = null;

        RegisteredClient.Builder builder =
                RegisteredClient
                        .withId(UUID.randomUUID().toString())
                        .clientId(clientId)
                        .clientName(request.clientName().trim());

        if (request.clientType()
                == OAuthClientType.CONFIDENTIAL) {

            rawClientSecret = generateClientSecret();

            builder
                    .clientSecret(
                            passwordEncoder.encode(
                                    rawClientSecret
                            )
                    )

                    .clientAuthenticationMethod(
                            ClientAuthenticationMethod
                                    .CLIENT_SECRET_BASIC
                    )

                    .authorizationGrantType(
                            AuthorizationGrantType
                                    .AUTHORIZATION_CODE
                    )

                    .authorizationGrantType(
                            AuthorizationGrantType
                                    .REFRESH_TOKEN
                    );

        } else {

            builder
                    .clientAuthenticationMethod(
                            ClientAuthenticationMethod.NONE
                    )

                    .authorizationGrantType(
                            AuthorizationGrantType
                                    .AUTHORIZATION_CODE
                    );
        }

        request.redirectUris()
                .forEach(builder::redirectUri);

        if (request.postLogoutRedirectUris() != null) {
            request.postLogoutRedirectUris()
                    .forEach(builder::postLogoutRedirectUri);
        }

        scopes.forEach(builder::scope);

        builder.clientSettings(
                ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(
                                request.requireAuthorizationConsent()
                        )
                        .build()
        );

        builder.tokenSettings(tokenSettings);

        RegisteredClient registeredClient =
                builder.build();

        registeredClientRepository.save(
                registeredClient
        );

        return new OAuthClientCreatedResponse(
                toResponse(registeredClient),
                rawClientSecret
        );
    }

    public OAuthClientResponse getByClientId(
            String clientId
    ) {

        return toResponse(
                getRegisteredClient(clientId)
        );
    }

    private OAuthClientResponse toResponse(
            RegisteredClient client
    ) {

        OAuthClientType clientType =
                client
                        .getClientAuthenticationMethods()
                        .contains(
                                ClientAuthenticationMethod.NONE
                        )
                        ? OAuthClientType.PUBLIC
                        : OAuthClientType.CONFIDENTIAL;

        Set<String> grantTypes =
                client
                        .getAuthorizationGrantTypes()
                        .stream()
                        .map(AuthorizationGrantType::getValue)
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        return new OAuthClientResponse(
                client.getClientId(),
                client.getClientName(),
                clientType,
                client.getRedirectUris(),
                client.getPostLogoutRedirectUris(),
                client.getScopes(),
                grantTypes,
                client.getClientSettings()
                        .isRequireProofKey(),
                client.getClientSettings()
                        .isRequireAuthorizationConsent()
        );
    }

    private String generateClientSecret() {

        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public List<OAuthClientResponse> list() {

        return managementRepository
                .findAllClientIds()
                .stream()
                .map(registeredClientRepository::findByClientId)
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OAuthClientResponse update(
            String clientId,
            UpdateOAuthClientRequest request
    ) {

        RegisteredClient existing =
                getRegisteredClient(clientId);

        Set<String> scopes =
                scopeService.validateAndNormalize(
                        request.scopes()
                );


        RegisteredClient updated =
                RegisteredClient
                        .from(existing)

                        .clientName(
                                request.clientName().trim()
                        )

                        .redirectUris(uris -> {
                            uris.clear();
                            uris.addAll(
                                    request.redirectUris()
                            );
                        })

                        .postLogoutRedirectUris(uris -> {
                            uris.clear();

                            if (request.postLogoutRedirectUris() != null) {
                                uris.addAll(
                                        request.postLogoutRedirectUris()
                                );
                            }
                        })

                        .scopes(currentScopes -> {
                            currentScopes.clear();
                            currentScopes.addAll(
                                    scopes
                            );
                        })

                        .clientSettings(
                                ClientSettings
                                        .withSettings(
                                                existing
                                                        .getClientSettings()
                                                        .getSettings()
                                        )
                                        .requireAuthorizationConsent(
                                                request.requireAuthorizationConsent()
                                        )
                                        .build()
                        )

                        .build();

        registeredClientRepository.save(updated);

        return toResponse(updated);
    }
    private RegisteredClient getRegisteredClient(
            String clientId
    ) {

        RegisteredClient client =
                registeredClientRepository
                        .findByClientId(clientId);

        if (client == null) {
            throw new ResourceNotFoundException(
                    "OAuth client not found"
            );
        }

        return client;
    }

    @Transactional
    public OAuthClientSecretResponse rotateSecret(
            String clientId
    ) {

        RegisteredClient client =
                getRegisteredClient(clientId);

        boolean publicClient =
                client
                        .getClientAuthenticationMethods()
                        .contains(
                                ClientAuthenticationMethod.NONE
                        );

        if (publicClient) {
            throw new InvalidRequestException(
                    "Public OAuth clients do not have a client secret"
            );
        }

        String rawSecret =
                generateClientSecret();

        String encodedSecret =
                passwordEncoder.encode(rawSecret);

        int updated =
                managementRepository.updateClientSecret(
                        client.getId(),
                        encodedSecret
                );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Failed to rotate OAuth client secret"
            );
        }

        return new OAuthClientSecretResponse(
                client.getClientId(),
                rawSecret
        );
    }

    @Transactional
    public void delete(
            String clientId
    ) {

        RegisteredClient client =
                getRegisteredClient(clientId);

        managementRepository.deleteConsents(
                client.getId()
        );

        managementRepository.deleteAuthorizations(
                client.getId()
        );

        int deleted =
                managementRepository.deleteClient(
                        client.getId()
                );

        if (deleted != 1) {
            throw new IllegalStateException(
                    "Failed to delete OAuth client"
            );
        }
    }
}