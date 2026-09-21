package io.github.mesubash.springbootoauth2authorizationserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.github.mesubash.springbootoauth2authorizationserver.security.ActiveAuthorizationFilter;
import io.github.mesubash.springbootoauth2authorizationserver.security.PemKeyLoader;
import io.github.mesubash.springbootoauth2authorizationserver.user.service.OidcUserClaimsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class AuthorizationServerConfig {

    // Security Chain for Oauth2 / OIDC protocol endpoints
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .oauth2AuthorizationServer(authorizationServer -> {
                    http.securityMatcher(
                            authorizationServer.getEndpointsMatcher()
                    )
                            .cors(Customizer.withDefaults()
                            );

                    authorizationServer
                            .oidc(Customizer.withDefaults());
                })

                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )

                .exceptionHandling(exceptions ->
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )

                );
        return http.build();
    }

    // Normal application security filter chain
    // this provides the login page used when the user reaches the authorization endpoint

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers("/api/v1/auth/register")
                )
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize ->
                    authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register"
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    //temp OauthClient
    //Later:
    //RegisteredClientRepository -> PostgreSQL.

    @Bean
    @DependsOn("flyway")
    public RegisteredClientRepository registeredClientRepository(
            JdbcTemplate jdbcTemplate
    ) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository
    ){
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    @Profile("!test")
    public JWKSource<SecurityContext> persistentJwkSource(
            PemKeyLoader pemKeyLoader,
            AuthorizationServerKeyProperties keyProperties
    ) {

        RSAKey rsaKey = pemKeyLoader.load(
                keyProperties.getPrivateKey(),
                keyProperties.getPublicKey()
        );

        return new ImmutableJWKSet<>(
                new JWKSet(rsaKey)
        );
    }
    @Bean
    @Profile("test")
    public JWKSource<SecurityContext> testJwkSource() {

        KeyPair keyPair = generateRsaKey();

        RSAPublicKey publicKey =
                (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey =
                (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey =
                new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .keyID(UUID.randomUUID().toString())
                        .build();

        return new ImmutableJWKSet<>(
                new JWKSet(rsaKey)
        );
    }

    private static KeyPair generateRsaKey() {

        try {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);

            return generator.generateKeyPair();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to generate RSA key pair",
                    exception
            );
        }
    }

    // required for OIDC endpoints such as UserInfo
    @Bean
    public JwtDecoder jwtDecoder(
            JWKSource<SecurityContext> jwkSource
    ) {

        return OAuth2AuthorizationServerConfiguration
                .jwtDecoder(jwkSource);
    }


    //config of this authorization server itself
    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${authorization-server.issuer}")
            String issuer
    ) {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public TokenSettings tokenSettings(){
        return TokenSettings.builder()
                .authorizationCodeTimeToLive(Duration.ofMinutes(5))
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(false)
                .build();
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(
                jdbcTemplate,
                registeredClientRepository
        );
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(
            OidcUserClaimsService oidcUserClaimsService
    ) {
        return context -> {

            /*
             * Access token
             */
            if (OAuth2TokenType.ACCESS_TOKEN.equals(
                    context.getTokenType()
            )) {

                Set<String> roles = context
                        .getPrincipal()
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .filter(authority ->
                                authority.startsWith("ROLE_")
                        )
                        .map(authority ->
                                authority.substring(
                                        "ROLE_".length()
                                )
                        )
                        .collect(Collectors.toSet());

                context.getClaims()
                        .claim("roles", roles);

                return;
            }


            /*
             * OpenID Connect ID Token
             */
            if (OidcParameterNames.ID_TOKEN.equals(
                    context.getTokenType().getValue()
            )) {

                Map<String, Object> userClaims =
                        oidcUserClaimsService.loadClaims(
                                context.getPrincipal().getName(),
                                context.getAuthorizedScopes()
                        );

                context.getClaims()
                        .claims(claims ->
                                claims.putAll(userClaims)
                        );
            }
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter scopeConverter =
                new JwtGrantedAuthoritiesConverter();

        JwtGrantedAuthoritiesConverter roleConverter =
                new JwtGrantedAuthoritiesConverter();

        roleConverter.setAuthoritiesClaimName("roles");
        roleConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                new DelegatingJwtGrantedAuthoritiesConverter(
                        scopeConverter,
                        roleConverter
                )
        );

        return converter;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain adminApiSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationsConverter,
            OAuth2AuthorizationService authorizationService
    ) throws Exception {

        http
                .securityMatcher(
                        "/api/v1/clients",
                        "/api/v1/clients/**",
                        "/api/v1/scopes",
                        "/api/v1/scopes/**",
                        "/api/v1/users",
                        "/api/v1/users/**",
                        "/api/v1/account/**"

                )

                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/api/v1/account/**")
                                .authenticated()
                                .anyRequest()
                                .hasRole("ADMIN")
                )

                .csrf(csrf ->
                        csrf.disable()
                )
                .cors(Customizer.withDefaults())
                .addFilterAfter(
                        new ActiveAuthorizationFilter(
                                authorizationService
                        ),
                        BearerTokenAuthenticationFilter.class
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationsConverter
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            AuthorizationServerCorsProperties properties
    ) {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                properties.getAllowedOrigins()
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Location"
                )
        );

        /*
         * We are not designing cross-origin cookie/session
         * authentication for APIs.
         */
        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L);


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }


}
