package io.github.mesubash.springbootoauth2authorizationserver.user.service;


import java.time.Instant;

import io.github.mesubash.springbootoauth2authorizationserver.config.LoginSecurityProperties;
import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {

    private final UserRepository userRepository;
    private final LoginSecurityProperties properties;

    public LoginAttemptService(
            UserRepository userRepository,
            LoginSecurityProperties properties
    ) {
        this.userRepository = userRepository;
        this.properties = properties;
    }


    @Transactional
    public void recordFailedLogin(
            String login
    ) {

        userRepository
                .findByUsernameOrEmail(
                        login,
                        login
                )
                .ifPresent(user -> {

                    /*
                     * Don't modify accounts that are already
                     * manually locked or temporarily locked.
                     */
                    if (!user.isAccountNonLocked()) {
                        return;
                    }

                    int attempts =
                            user.getFailedLoginAttempts() + 1;

                    user.setFailedLoginAttempts(
                            attempts
                    );

                    if (attempts
                            >= properties.getMaxFailedAttempts()) {

                        user.setAccountNonLocked(false);

                        user.setLockedUntil(
                                Instant.now().plus(
                                        properties.getLockDuration()
                                )
                        );
                    }

                    userRepository.save(user);
                });
    }


    @Transactional
    public void recordSuccessfulLogin(
            String username
    ) {

        userRepository
                .findByUsername(username)
                .ifPresent(user -> {

                    if (user.getFailedLoginAttempts() == 0
                            && user.getLockedUntil() == null) {
                        return;
                    }

                    user.setFailedLoginAttempts(0);
                    user.setLockedUntil(null);

                    userRepository.save(user);
                });
    }


    @Transactional
    public UserEntity releaseExpiredTemporaryLock(
            UserEntity user
    ) {

        Instant lockedUntil =
                user.getLockedUntil();

        if (user.isAccountNonLocked()) {
            return user;
        }

        /*
         * null means an administrator manually locked
         * the account. Never automatically unlock it.
         */
        if (lockedUntil == null) {
            return user;
        }

        if (lockedUntil.isAfter(Instant.now())) {
            return user;
        }

        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);

        return userRepository.save(user);
    }
}