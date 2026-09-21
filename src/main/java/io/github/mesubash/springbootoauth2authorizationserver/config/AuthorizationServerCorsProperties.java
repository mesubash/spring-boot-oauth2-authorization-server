package io.github.mesubash.springbootoauth2authorizationserver.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "authorization-server.cors"
)
public class AuthorizationServerCorsProperties {

    private List<String> allowedOrigins =
            new ArrayList<>();

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(
            List<String> allowedOrigins
    ) {
        this.allowedOrigins = allowedOrigins;
    }
}