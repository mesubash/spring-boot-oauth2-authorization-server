package io.github.mesubash.springbootoauth2authorizationserver.scope;

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
class OAuthScopeManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldAllowAdminToCreateScope()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/scopes")

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
                                          "name": "reports.read",
                                          "description": "Read reports"
                                        }
                                        """)
                )

                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        jsonPath("$.name")
                                .value("reports.read")
                )

                .andExpect(
                        jsonPath("$.enabled")
                                .value(true)
                );
    }


    @Test
    void shouldRejectReservedOidcScope()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/scopes")

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
                                          "name": "openid",
                                          "description": "Invalid"
                                        }
                                        """)
                )

                .andExpect(
                        status().isConflict()
                );
    }


    @Test
    void shouldRejectNonAdmin()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/scopes")

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