package io.github.mesubash.springbootoauth2authorizationserver.user;

import java.time.Instant;
import java.util.UUID;

import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginLockoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;


    @Test
    void shouldTemporarilyLockUserAfterFiveFailedLogins()
            throws Exception {

        String suffix =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        String username =
                "lock-" + suffix;

        String email =
                username + "@example.com";

        String password =
                "CorrectPassword123!";


        register(
                username,
                email,
                password
        );


        for (int i = 0; i < 5; i++) {

            mockMvc.perform(
                            formLogin()
                                    .user(username)
                                    .password("wrong-password")
                    )

                    .andExpect(
                            unauthenticated()
                    );
        }


        UserEntity lockedUser =
                userRepository
                        .findByUsername(username)
                        .orElseThrow();


        assertThat(
                lockedUser.isAccountNonLocked()
        ).isFalse();

        assertThat(
                lockedUser.getFailedLoginAttempts()
        ).isEqualTo(5);

        assertThat(
                lockedUser.getLockedUntil()
        ).isAfter(Instant.now());


        /*
         * Correct password still cannot authenticate
         * while temporary lock is active.
         */
        mockMvc.perform(
                        formLogin()
                                .user(username)
                                .password(password)
                )

                .andExpect(
                        unauthenticated()
                );


        /*
         * Simulate lock expiration.
         */
        lockedUser.setLockedUntil(
                Instant.now().minusSeconds(60)
        );

        userRepository.save(
                lockedUser
        );


        mockMvc.perform(
                        formLogin()
                                .user(username)
                                .password(password)
                )

                .andExpect(
                        authenticated()
                                .withUsername(username)
                );


        UserEntity unlockedUser =
                userRepository
                        .findByUsername(username)
                        .orElseThrow();


        assertThat(
                unlockedUser.isAccountNonLocked()
        ).isTrue();

        assertThat(
                unlockedUser.getFailedLoginAttempts()
        ).isZero();

        assertThat(
                unlockedUser.getLockedUntil()
        ).isNull();
    }


    private void register(
            String username,
            String email,
            String password
    ) throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "username": "%s",
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """
                                                .formatted(
                                                        username,
                                                        email,
                                                        password
                                                )
                                )
                )

                .andExpect(
                        status().isCreated()
                );
    }
}