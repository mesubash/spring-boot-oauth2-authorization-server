package io.github.mesubash.springbootoauth2authorizationserver.user.service;

import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.mesubash.springbootoauth2authorizationserver.common.exception.ResourceNotFoundException;
import io.github.mesubash.springbootoauth2authorizationserver.user.dto.AdminUserResponse;
import io.github.mesubash.springbootoauth2authorizationserver.user.dto.UpdateUserStatusRequest;
import io.github.mesubash.springbootoauth2authorizationserver.user.entity.RoleEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.RoleRepository;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserAdminService(
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


    @Transactional(readOnly = true)
    public Page<AdminUserResponse> list(
            Pageable pageable
    ) {

        return userRepository
                .findAll(pageable)
                .map(this::toResponse);
    }


    @Transactional(readOnly = true)
    public AdminUserResponse get(
            UUID userId
    ) {

        return toResponse(
                getUser(userId)
        );
    }


    @Transactional
    public AdminUserResponse updateStatus(
            UUID userId,
            UpdateUserStatusRequest request
    ) {

        UserEntity user =
                getUser(userId);

        user.setEnabled(
                request.enabled()
        );

        user.setAccountNonExpired(
                request.accountNonExpired()
        );

        user.setAccountNonLocked(
                request.accountNonLocked()
        );

        user.setCredentialsNonExpired(
                request.credentialsNonExpired()
        );

        return toResponse(
                userRepository.save(user)
        );
    }


    @Transactional
    public AdminUserResponse addRole(
            UUID userId,
            String roleName
    ) {

        UserEntity user =
                getUser(userId);

        RoleEntity role =
                roleRepository
                        .findByName(
                                roleName.toUpperCase()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Role not found"
                                )
                        );

        boolean alreadyAssigned =
                user.getRoles()
                        .stream()
                        .anyMatch(existing ->
                                existing
                                        .getName()
                                        .equals(role.getName())
                        );

        if (!alreadyAssigned) {
            user.getRoles().add(role);
        }

        return toResponse(
                userRepository.save(user)
        );
    }


    @Transactional
    public AdminUserResponse removeRole(
            UUID userId,
            String roleName
    ) {

        UserEntity user =
                getUser(userId);

        boolean removed =
                user.getRoles()
                        .removeIf(role ->
                                role.getName()
                                        .equalsIgnoreCase(
                                                roleName
                                        )
                        );

        if (!removed) {
            throw new ResourceNotFoundException(
                    "Role is not assigned to user"
            );
        }

        return toResponse(
                userRepository.save(user)
        );
    }


    private UserEntity getUser(
            UUID userId
    ) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }


    private AdminUserResponse toResponse(
            UserEntity user
    ) {

        LinkedHashSet<String> roles =
                user.getRoles()
                        .stream()
                        .map(RoleEntity::getName)
                        .sorted()
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),

                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired(),

                roles
        );
    }
}
