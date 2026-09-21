package io.github.mesubash.springbootoauth2authorizationserver.user;


import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    void shouldRegisterUser() throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "username": "integration-user",
                                          "email": "integration@example.com",
                                          "password": "Password123!"
                                        }
                                        """)
                )

                .andExpect(status().isCreated())

                .andExpect(
                        jsonPath("$.username")
                                .value("integration-user")
                )

                .andExpect(
                        jsonPath("$.email")
                                .value("integration@example.com")
                )

                .andExpect(
                        jsonPath("$.id").exists()
                );


        UserEntity user = userRepository
                .findByUsername("integration-user")
                .orElseThrow();


        assertThat(
                passwordEncoder.matches(
                        "Password123!",
                        user.getPassword()
                )
        ).isTrue();


        assertThat(
                user.getRoles()
                        .stream()
                        .anyMatch(role ->
                                role.getName().equals("USER")
                        )
        ).isTrue();
    }

    @Test
    void shouldRejectDuplicateUsername() throws Exception {

        String firstRequest = """
            {
              "username": "duplicate-user",
              "email": "first@example.com",
              "password": "Password123!"
            }
            """;

        String secondRequest = """
            {
              "username": "duplicate-user",
              "email": "second@example.com",
              "password": "Password123!"
            }
            """;


        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest)
        ).andExpect(
                status().isCreated()
        );


        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(secondRequest)
                )

                .andExpect(
                        status().isConflict()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Username is already registered")
                );
    }

    @Test
    void shouldRejectInvalidRegistration() throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "username": "a",
                                      "email": "invalid-email",
                                      "password": "123"
                                    }
                                    """)
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("Validation failed")
                )

                .andExpect(
                        jsonPath("$.validationErrors.username")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.validationErrors.email")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.validationErrors.password")
                                .exists()
                );
    }
}