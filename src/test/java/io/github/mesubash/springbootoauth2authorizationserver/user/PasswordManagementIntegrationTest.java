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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldRejectUnauthenticatedPasswordChange()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/account/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "old-password",
                                  "newPassword": "NewPassword123!"
                                }
                                """)
        ).andExpect(
                status().isUnauthorized()
        );
    }


    @Test
    void shouldRejectNonAdminPasswordReset()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/v1/users/00000000-0000-0000-0000-000000000999/password"
                        )

                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "ROLE_USER"
                                                )
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content("""
                                        {
                                          "newPassword": "NewPassword123!"
                                        }
                                        """)
                )

                .andExpect(
                        status().isForbidden()
                );
    }
}