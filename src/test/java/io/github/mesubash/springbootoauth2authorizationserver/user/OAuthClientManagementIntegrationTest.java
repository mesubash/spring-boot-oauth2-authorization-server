package io.github.mesubash.springbootoauth2authorizationserver.user;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OAuthClientManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldRejectRequestWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/clients")
        ).andExpect(
                status().isUnauthorized()
        );
    }


    @Test
    void shouldAllowAdminToCreateOAuthClient()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/clients")

                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_ADMIN"
                                                )
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content("""
                                        {
                                          "clientName": "Integration Client",
                                          "clientType": "CONFIDENTIAL",
                                          "redirectUris": [
                                            "http://127.0.0.1:3000/callback"
                                          ],
                                          "postLogoutRedirectUris": [
                                            "http://127.0.0.1:3000"
                                          ],
                                          "scopes": [
                                            "openid",
                                            "profile",
                                            "read"
                                          ],
                                          "requireAuthorizationConsent": true
                                        }
                                        """)
                )

                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        jsonPath("$.client.clientId")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.client.clientName")
                                .value("Integration Client")
                )

                .andExpect(
                        jsonPath("$.client.clientType")
                                .value("CONFIDENTIAL")
                )

                .andExpect(
                        jsonPath("$.clientSecret")
                                .isNotEmpty()
                );
    }
    @Test
    void shouldRejectNonAdminUser()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/clients")

                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_USER"
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isForbidden()
                );
    }
}