package io.github.mesubash.springbootoauth2authorizationserver.security;


import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperationalSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void healthEndpointShouldBePublic()
            throws Exception {

        mockMvc.perform(
                        get("/actuator/health")
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value("UP")
                );
    }


    @Test
    void auditEndpointShouldRejectUnauthenticatedRequest()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/audit-events")
        ).andExpect(
                status().isUnauthorized()
        );
    }


    @Test
    void auditEndpointShouldRejectNonAdmin()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/audit-events")

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


    @Test
    void auditEndpointShouldAllowAdmin()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/audit-events")

                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_ADMIN"
                                                )
                                        )
                                )
                )

                .andExpect(
                        status().isOk()
                );
    }
}