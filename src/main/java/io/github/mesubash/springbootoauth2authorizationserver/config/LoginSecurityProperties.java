package io.github.mesubash.springbootoauth2authorizationserver.config;


import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = "authorization-server.security.login"
)
public class LoginSecurityProperties {

    private int maxFailedAttempts = 5;

    private Duration lockDuration =
            Duration.ofMinutes(15);

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(
            int maxFailedAttempts
    ) {
        this.maxFailedAttempts =
                maxFailedAttempts;
    }

    public Duration getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(
            Duration lockDuration
    ) {
        this.lockDuration =
                lockDuration;
    }
}