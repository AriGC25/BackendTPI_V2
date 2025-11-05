package com.transportista.tarifas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html", "/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

                        // Clientes: OPERADOR puede hacer todo, CLIENTE puede registrarse y ver su propio perfil
                        .requestMatchers(HttpMethod.POST, "/clientes").hasAnyRole("OPERADOR", "CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/clientes/**").hasAnyRole("OPERADOR", "CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/clientes/**").hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.DELETE, "/clientes/**").hasRole("OPERADOR")

                        // Tarifas: OPERADOR puede gestionar, CLIENTE puede consultar
                        .requestMatchers(HttpMethod.GET, "/tarifas/**").hasAnyRole("OPERADOR", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/tarifas/**").hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/tarifas/**").hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.DELETE, "/tarifas/**").hasRole("OPERADOR")

                        // Cálculos: solo OPERADOR
                        .requestMatchers("/calculos/**").hasRole("OPERADOR")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(validators);
        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }

    @Bean
    public org.springframework.core.convert.converter.Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            List<String> roles = realmAccess != null ? (List<String>) realmAccess.get("roles") : Collections.emptyList();

            List<GrantedAuthority> authorities = roles.stream()
                    .map(r -> "ROLE_" + r.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            return new JwtAuthenticationToken(jwt, authorities);
        };
    }
}