package io.github.mesubash.springbootoauth2authorizationserver.user;


import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldRejectUnauthenticatedUser()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users")
        ).andExpect(
                status().isUnauthorized()
        );
    }


    @Test
    void shouldRejectNonAdminUser()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users")

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
    void shouldAllowAdminToListUsers()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/users")

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
