package io.github.mesubash.springbootoauth2authorizationserver.user.service;

import java.util.UUID;

import io.github.mesubash.springbootoauth2authorizationserver.common.exception.InvalidCredentialsException;
import io.github.mesubash.springbootoauth2authorizationserver.common.exception.InvalidRequestException;
import io.github.mesubash.springbootoauth2authorizationserver.common.exception.ResourceNotFoundException;
import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserSecurityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordService {

    private final UserRepository userRepository;
    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordService(
            UserRepository userRepository,
            UserSecurityRepository userSecurityRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public void changePassword(
            String username,
            String currentPassword,
            String newPassword
    ) {

        UserEntity user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        )) {

            throw new InvalidCredentialsException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {

            throw new InvalidRequestException(
                    "New password must be different from current password"
            );
        }

        updatePassword(
                user,
                newPassword
        );
    }


    @Transactional
    public void resetPassword(
            UUID userId,
            String newPassword
    ) {

        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {

            throw new InvalidRequestException(
                    "New password must be different from current password"
            );
        }

        updatePassword(
                user,
                newPassword
        );
    }


    private void updatePassword(
            UserEntity user,
            String newPassword
    ) {

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        user.setCredentialsNonExpired(true);

        userRepository.save(user);

        userSecurityRepository
                .deleteAuthorizationsByPrincipalName(
                        user.getUsername()
                );
    }
}