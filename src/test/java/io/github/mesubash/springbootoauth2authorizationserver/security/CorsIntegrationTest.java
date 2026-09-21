package io.github.mesubash.springbootoauth2authorizationserver.security;


import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void shouldAllowConfiguredOrigin()
            throws Exception {

        mockMvc.perform(
                        options("/api/v1/clients")
                                .header(
                                        "Origin",
                                        "http://localhost:5173"
                                )
                                .header(
                                        "Access-Control-Request-Method",
                                        "GET"
                                )
                                .header(
                                        "Access-Control-Request-Headers",
                                        "Authorization"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        header().string(
                                "Access-Control-Allow-Origin",
                                "http://localhost:5173"
                        )
                );
    }


    @Test
    void shouldRejectUnknownOrigin()
            throws Exception {

        mockMvc.perform(
                        options("/api/v1/clients")
                                .header(
                                        "Origin",
                                        "https://evil.example"
                                )
                                .header(
                                        "Access-Control-Request-Method",
                                        "GET"
                                )
                )

                .andExpect(
                        header().doesNotExist(
                                "Access-Control-Allow-Origin"
                        )
                );
    }


    @Test
    void shouldAllowTokenEndpointFromConfiguredOrigin()
            throws Exception {

        mockMvc.perform(
                        options("/oauth2/token")
                                .header(
                                        "Origin",
                                        "http://localhost:5173"
                                )
                                .header(
                                        "Access-Control-Request-Method",
                                        "POST"
                                )
                                .header(
                                        "Access-Control-Request-Headers",
                                        "Content-Type,Authorization"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        header().string(
                                "Access-Control-Allow-Origin",
                                "http://localhost:5173"
                        )
                );
    }
}