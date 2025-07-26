package com.star.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error; // <--- ADD THIS IMPORT
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult; // <--- ADD THIS IMPORT
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.cors(cors -> {
        });

        return http.build();
    }

    /**
     * Configures the JwtDecoder to validate Google ID Tokens.
     * This bean is responsible for:
     * 1. Fetching Google's public keys from the JWK Set URI.
     * 2. Verifying the JWT's signature using these keys.
     * 3. Validating the 'issuer' (iss) and 'audience' (aud) claims.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        jwtDecoder.setJwtValidator(
                jwt -> {
                    // Validate the issuer (who issued the token).
                    if (!jwt.getIssuer().toString().equals("https://accounts.google.com")) {
                        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_issuer", "Invalid issuer: " + jwt.getIssuer(), null));
                    }

                    // Validate the audience (who the token is for).
                    // The audience claim in the JWT must contain your Google Client ID.
                    // It's good practice to check for null audience first to avoid NullPointerExceptions.
                    if (jwt.getAudience() == null || !jwt.getAudience().contains(googleClientId)) {
                        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_audience", "Invalid audience: " + jwt.getAudience(), null));
                    }

                    return OAuth2TokenValidatorResult.success(); // <--- CORRECT RETURN TYPE FOR SUCCESS
                }
        );
        return jwtDecoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return authorities;
        });
        return converter;
    }
}

