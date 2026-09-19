package io.github.mesubash.springbootoauth2authorizationserver.config;

import io.github.mesubash.springbootoauth2authorizationserver.user.entity.RoleEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.RoleRepository;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevelopmentDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentDataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        RoleEntity userRole = roleRepository
                .findByName("USER")
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setName("USER");
                    role.setDescription("Default user role");

                    return roleRepository.save(role);
                });

        if (userRepository.findByUsername("user").isEmpty()) {

            UserEntity user = new UserEntity();

            user.setUsername("user");
            user.setEmail("user@example.com");
            user.setPassword(
                    passwordEncoder.encode("password")
            );
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);

            user.getRoles().add(userRole);

            userRepository.save(user);
        }
    }
}
