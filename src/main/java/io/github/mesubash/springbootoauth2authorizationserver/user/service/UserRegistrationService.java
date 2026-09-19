package io.github.mesubash.springbootoauth2authorizationserver.user.service;


import java.util.Locale;

import io.github.mesubash.springbootoauth2authorizationserver.user.dto.RegisterUserRequest;
import io.github.mesubash.springbootoauth2authorizationserver.user.dto.UserResponse;
import io.github.mesubash.springbootoauth2authorizationserver.user.entity.RoleEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.RoleRepository;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {

        String username = request.username().trim();

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already registered"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        RoleEntity userRole = roleRepository
                .findByName("USER")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Default USER role is not configured"
                        )
                );

        UserEntity user = new UserEntity();

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.password())
        );

        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        user.getRoles().add(userRole);

        UserEntity savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }
}