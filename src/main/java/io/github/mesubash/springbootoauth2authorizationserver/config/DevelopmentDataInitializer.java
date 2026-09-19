package io.github.mesubash.springbootoauth2authorizationserver.config;

import io.github.mesubash.springbootoauth2authorizationserver.user.entity.RoleEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.entity.UserEntity;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.RoleRepository;
import io.github.mesubash.springbootoauth2authorizationserver.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("dev")
public class DevelopmentDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisteredClientRepository registeredClientRepository;
    private final TokenSettings tokenSettings;

    public DevelopmentDataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            RegisteredClientRepository registeredClientRepository,
            TokenSettings tokenSettings
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.registeredClientRepository = registeredClientRepository;
        this.tokenSettings = tokenSettings;
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
            initializeDemoClient();
        }
    }

    private void initializeDemoClient() {

        if (registeredClientRepository.findByClientId("demo-client") != null) {
            return;
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.randomUUID().toString())

                .clientId("demo-client")
                .clientSecret(
                        passwordEncoder.encode("demo-secret")
                )

                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )

                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .authorizationGrantType(
                        AuthorizationGrantType.REFRESH_TOKEN
                )

                .redirectUri(
                        "http://127.0.0.1:8081/callback"
                )

                .postLogoutRedirectUri(
                        "http://127.0.0.1:8081/"
                )

                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("read")
                .scope("write")

                .clientSettings(
                        ClientSettings.builder()
                                .requireProofKey(true)
                                .requireAuthorizationConsent(true)
                                .build()
                )

                .tokenSettings(tokenSettings)

                .build();

        registeredClientRepository.save(client);
    }
}
