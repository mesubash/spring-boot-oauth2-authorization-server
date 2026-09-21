package io.github.mesubash.springbootoauth2authorizationserver.oauth;


import com.jayway.jsonpath.JsonPath;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OAuthProtocolIntegrationTest {

    private static final String REDIRECT_URI =
            "http://127.0.0.1:8081/callback";

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegisteredClientRepository registeredClientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenSettings tokenSettings;

    private TestClient createTestClient() {

        String clientId =
                "test-client-" + UUID.randomUUID();

        String clientSecret =
                generateRandomValue();

        RegisteredClient client =
                RegisteredClient
                        .withId(UUID.randomUUID().toString())

                        .clientId(clientId)

                        .clientSecret(
                                passwordEncoder.encode(clientSecret)
                        )

                        .clientAuthenticationMethod(
                                ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                        )

                        .authorizationGrantType(
                                AuthorizationGrantType.AUTHORIZATION_CODE
                        )

                        .authorizationGrantType(
                                AuthorizationGrantType.REFRESH_TOKEN
                        )

                        .redirectUri(REDIRECT_URI)

                        .scope("read")

                        .clientSettings(
                                ClientSettings.builder()
                                        .requireProofKey(true)

                                        // Important for protocol tests.
                                        .requireAuthorizationConsent(false)

                                        .build()
                        )

                        .tokenSettings(tokenSettings)

                        .build();

        registeredClientRepository.save(client);

        return new TestClient(
                clientId,
                clientSecret
        );
    }

    private record TestClient(
            String clientId,
            String clientSecret
    ) {
    }

    private String generateCodeVerifier() {
        return generateRandomValue();
    }

    private String generateCodeChallenge(
            String codeVerifier
    ) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                        codeVerifier.getBytes(StandardCharsets.US_ASCII)
                );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }

    private String generateRandomValue() {

        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String authorize(
            TestClient client,
            String codeChallenge
    ) throws Exception {

        // Authorization endpoint validates the real query string,
        // so parameters must be in the URI, not added via .param().
        URI authorizeUri =
                UriComponentsBuilder
                        .fromPath("/oauth2/authorize")
                        .queryParam("response_type", "code")
                        .queryParam("client_id", client.clientId())
                        .queryParam("redirect_uri", REDIRECT_URI)
                        .queryParam("scope", "read")
                        .queryParam("state", generateRandomValue())
                        .queryParam("code_challenge", codeChallenge)
                        .queryParam("code_challenge_method", "S256")
                        .encode()
                        .build()
                        .toUri();

        MvcResult result =
                mockMvc.perform(
                                get(authorizeUri)

                                        .with(
                                                user("test-user")
                                        )
                        )

                        .andExpect(
                                status().is3xxRedirection()
                        )

                        .andReturn();

        String location =
                result.getResponse()
                        .getHeader("Location");

        assertThat(location).isNotBlank();

        URI redirect =
                URI.create(location);

        String code =
                UriComponentsBuilder
                        .fromUri(redirect)
                        .build()
                        .getQueryParams()
                        .getFirst("code");

        assertThat(code).isNotBlank();

        return code;
    }

    @Test
    void shouldExchangeAuthorizationCodeUsingPkce()
            throws Exception {

        TestClient client =
                createTestClient();

        String verifier =
                generateCodeVerifier();

        String challenge =
                generateCodeChallenge(verifier);


        String code =
                authorize(
                        client,
                        challenge
                );


        mockMvc.perform(
                        post("/oauth2/token")

                                .with(
                                        httpBasic(
                                                client.clientId(),
                                                client.clientSecret()
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )

                                .param(
                                        "grant_type",
                                        "authorization_code"
                                )

                                .param(
                                        "code",
                                        code
                                )

                                .param(
                                        "redirect_uri",
                                        REDIRECT_URI
                                )

                                .param(
                                        "code_verifier",
                                        verifier
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.access_token")
                                .isNotEmpty()
                )

                .andExpect(
                        jsonPath("$.refresh_token")
                                .isNotEmpty()
                )

                .andExpect(
                        jsonPath("$.token_type")
                                .value("Bearer")
                );
    }

    @Test
    void shouldRejectIncorrectPkceVerifier()
            throws Exception {

        TestClient client =
                createTestClient();

        String verifier =
                generateCodeVerifier();

        String challenge =
                generateCodeChallenge(verifier);


        String code =
                authorize(
                        client,
                        challenge
                );


        String incorrectVerifier =
                generateCodeVerifier();


        mockMvc.perform(
                        post("/oauth2/token")

                                .with(
                                        httpBasic(
                                                client.clientId(),
                                                client.clientSecret()
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )

                                .param(
                                        "grant_type",
                                        "authorization_code"
                                )

                                .param(
                                        "code",
                                        code
                                )

                                .param(
                                        "redirect_uri",
                                        REDIRECT_URI
                                )

                                .param(
                                        "code_verifier",
                                        incorrectVerifier
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.error")
                                .value("invalid_grant")
                );
    }

    @Test
    void shouldRejectReusedAuthorizationCode()
            throws Exception {

        TestClient client =
                createTestClient();

        String verifier =
                generateCodeVerifier();

        String challenge =
                generateCodeChallenge(verifier);

        String code =
                authorize(
                        client,
                        challenge
                );


        mockMvc.perform(
                tokenRequest(
                        client,
                        code,
                        verifier
                )
        ).andExpect(
                status().isOk()
        );


        mockMvc.perform(
                        tokenRequest(
                                client,
                                code,
                                verifier
                        )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.error")
                                .value("invalid_grant")
                );
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder tokenRequest(
            TestClient client,
            String code,
            String verifier
    ) {

        return post("/oauth2/token")

                .with(
                        httpBasic(
                                client.clientId(),
                                client.clientSecret()
                        )
                )

                .contentType(
                        MediaType.APPLICATION_FORM_URLENCODED
                )

                .param(
                        "grant_type",
                        "authorization_code"
                )

                .param(
                        "code",
                        code
                )

                .param(
                        "redirect_uri",
                        REDIRECT_URI
                )

                .param(
                        "code_verifier",
                        verifier
                );
    }
    @Test
    void shouldRefreshAccessToken()
            throws Exception {

        TestClient client =
                createTestClient();

        String verifier =
                generateCodeVerifier();

        String challenge =
                generateCodeChallenge(verifier);

        String code =
                authorize(
                        client,
                        challenge
                );


        MvcResult tokenResult =
                mockMvc.perform(
                                tokenRequest(
                                        client,
                                        code,
                                        verifier
                                )
                        )

                        .andExpect(
                                status().isOk()
                        )

                        .andReturn();


        String response =
                tokenResult
                        .getResponse()
                        .getContentAsString();


        String refreshToken =
                JsonPath.read(
                        response,
                        "$.refresh_token"
                );


        assertThat(refreshToken)
                .isNotBlank();


        MvcResult refreshResult =
                mockMvc.perform(
                                post("/oauth2/token")

                                        .with(
                                                httpBasic(
                                                        client.clientId(),
                                                        client.clientSecret()
                                                )
                                        )

                                        .contentType(
                                                MediaType.APPLICATION_FORM_URLENCODED
                                        )

                                        .param(
                                                "grant_type",
                                                "refresh_token"
                                        )

                                        .param(
                                                "refresh_token",
                                                refreshToken
                                        )
                        )

                        .andExpect(
                                status().isOk()
                        )

                        .andExpect(
                                jsonPath("$.access_token")
                                        .isNotEmpty()
                        )

                        .andExpect(
                                jsonPath("$.refresh_token")
                                        .isNotEmpty()
                        )

                        .andReturn();

        String refreshedResponse =
                refreshResult
                        .getResponse()
                        .getContentAsString();

        String newRefreshToken =
                JsonPath.read(
                        refreshedResponse,
                        "$.refresh_token"
                );

        // reuseRefreshTokens(false) -> refresh token must rotate.
        assertThat(newRefreshToken)
                .isNotEqualTo(refreshToken);
    }
    @Test
    void shouldIntrospectActiveAccessToken()
            throws Exception {

        TestClient client =
                createTestClient();

        String verifier =
                generateCodeVerifier();

        String challenge =
                generateCodeChallenge(verifier);

        String code =
                authorize(
                        client,
                        challenge
                );

        MvcResult tokenResult =
                mockMvc.perform(
                                tokenRequest(
                                        client,
                                        code,
                                        verifier
                                )
                        )
                        .andExpect(
                                status().isOk()
                        )
                        .andReturn();

        String tokenResponse =
                tokenResult
                        .getResponse()
                        .getContentAsString();

        String accessToken =
                JsonPath.read(
                        tokenResponse,
                        "$.access_token"
                );


        mockMvc.perform(
                        post("/oauth2/introspect")

                                .with(
                                        httpBasic(
                                                client.clientId(),
                                                client.clientSecret()
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )

                                .param(
                                        "token",
                                        accessToken
                                )

                                .param(
                                        "token_type_hint",
                                        "access_token"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.active")
                                .value(true)
                );
    }
    @Test
    void shouldRevokeAccessToken()
            throws Exception {

        TestClient client =
                createTestClient();

        String verifier =
                generateCodeVerifier();

        String challenge =
                generateCodeChallenge(verifier);

        String code =
                authorize(
                        client,
                        challenge
                );


        MvcResult tokenResult =
                mockMvc.perform(
                                tokenRequest(
                                        client,
                                        code,
                                        verifier
                                )
                        )

                        .andExpect(
                                status().isOk()
                        )

                        .andReturn();


        String tokenResponse =
                tokenResult
                        .getResponse()
                        .getContentAsString();

        String accessToken =
                JsonPath.read(
                        tokenResponse,
                        "$.access_token"
                );


        /*
         * Revoke token.
         */
        mockMvc.perform(
                        post("/oauth2/revoke")

                                .with(
                                        httpBasic(
                                                client.clientId(),
                                                client.clientSecret()
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )

                                .param(
                                        "token",
                                        accessToken
                                )

                                .param(
                                        "token_type_hint",
                                        "access_token"
                                )
                )

                .andExpect(
                        status().isOk()
                );


        /*
         * Introspection now reports inactive.
         */
        mockMvc.perform(
                        post("/oauth2/introspect")

                                .with(
                                        httpBasic(
                                                client.clientId(),
                                                client.clientSecret()
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )

                                .param(
                                        "token",
                                        accessToken
                                )

                                .param(
                                        "token_type_hint",
                                        "access_token"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.active")
                                .value(false)
                );
    }
    @Test
    void shouldRejectRevokedRefreshToken()
            throws Exception {

        TestClient client =
                createTestClient();

        String verifier =
                generateCodeVerifier();

        String challenge =
                generateCodeChallenge(verifier);

        String code =
                authorize(
                        client,
                        challenge
                );


        MvcResult tokenResult =
                mockMvc.perform(
                                tokenRequest(
                                        client,
                                        code,
                                        verifier
                                )
                        )

                        .andExpect(
                                status().isOk()
                        )

                        .andReturn();


        String tokenResponse =
                tokenResult
                        .getResponse()
                        .getContentAsString();

        String refreshToken =
                JsonPath.read(
                        tokenResponse,
                        "$.refresh_token"
                );


        mockMvc.perform(
                        post("/oauth2/revoke")

                                .with(
                                        httpBasic(
                                                client.clientId(),
                                                client.clientSecret()
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )

                                .param(
                                        "token",
                                        refreshToken
                                )

                                .param(
                                        "token_type_hint",
                                        "refresh_token"
                                )
                )

                .andExpect(
                        status().isOk()
                );


        mockMvc.perform(
                        post("/oauth2/token")

                                .with(
                                        httpBasic(
                                                client.clientId(),
                                                client.clientSecret()
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_FORM_URLENCODED
                                )

                                .param(
                                        "grant_type",
                                        "refresh_token"
                                )

                                .param(
                                        "refresh_token",
                                        refreshToken
                                )
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.error")
                                .value("invalid_grant")
                );
    }

}
