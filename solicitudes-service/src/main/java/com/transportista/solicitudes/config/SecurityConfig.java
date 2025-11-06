package com.transportista.solicitudes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
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

                        // === SOLICITUDES ===
                        // Crear solicitud: CLIENTE y OPERADOR
                        .requestMatchers(HttpMethod.POST, "/solicitudes", "/solicitudes/**").hasAnyRole("CLIENTE", "OPERADOR")

                        // Consultar solicitudes individuales: TODOS los roles autenticados
                        .requestMatchers(HttpMethod.GET, "/solicitudes/*", "/solicitudes/numero/**", "/solicitudes/cliente/**",
                                "/solicitudes/*/costo", "/solicitudes/*/tiempo-estimado")
                        .hasAnyRole("CLIENTE", "OPERADOR", "TRANSPORTISTA")

                        // Listar todas y filtros: solo OPERADOR
                        .requestMatchers(HttpMethod.GET, "/solicitudes", "/solicitudes/estado/**").hasRole("OPERADOR")

                        // Actualizar estado: solo OPERADOR
                        .requestMatchers(HttpMethod.PUT, "/solicitudes/**").hasRole("OPERADOR")

                        // === RUTAS ===
                        .requestMatchers(HttpMethod.GET, "/rutas/**").hasAnyRole("OPERADOR", "CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/rutas/**").hasRole("OPERADOR")

                        // === TRAMOS ===
                        .requestMatchers(HttpMethod.GET, "/tramos", "/tramos/*", "/tramos/transportista/**")
                        .hasAnyRole("OPERADOR", "TRANSPORTISTA")
                        .requestMatchers(HttpMethod.POST, "/tramos", "/tramos/**").hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/tramos/*/asignar-camion").hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/tramos/*/iniciar", "/tramos/*/finalizar")
                        .hasAnyRole("OPERADOR", "TRANSPORTISTA")

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
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }

    static class KeycloakRoleConverter implements org.springframework.core.convert.converter.Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) {
                return Collections.emptyList();
            }

            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                return Collections.emptyList();
            }

            return roles.stream()
                    .map(role -> "ROLE_" + role.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }
}
