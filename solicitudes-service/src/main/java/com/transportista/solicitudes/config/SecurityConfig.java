package com.transportista.solicitudes.config;

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
                        // Endpoints públicos (documentación, health checks)
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html", "/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

                        // Crear solicitud: CLIENTE y OPERADOR
                        .requestMatchers(HttpMethod.POST, "/solicitudes").hasAnyRole("CLIENTE", "OPERADOR")

                        // Consultar solicitudes individuales: CLIENTE, OPERADOR y TRANSPORTISTA
                        .requestMatchers(HttpMethod.GET, "/solicitudes/{id}", "/solicitudes/numero/**", "/solicitudes/cliente/**")
                        .hasAnyRole("CLIENTE", "OPERADOR", "TRANSPORTISTA")

                        // Listar todas las solicitudes y filtros: solo OPERADOR
                        .requestMatchers(HttpMethod.GET, "/solicitudes", "/solicitudes/estado/**").hasRole("OPERADOR")

                        // Rutas y asignaciones: OPERADOR y TRANSPORTISTA
                        .requestMatchers(HttpMethod.GET, "/rutas/**").hasAnyRole("OPERADOR", "TRANSPORTISTA")
                        .requestMatchers(HttpMethod.POST, "/rutas/**").hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/rutas/**").hasRole("OPERADOR")

                        // Tramos: OPERADOR puede gestionar, TRANSPORTISTA puede actualizar estado
                        .requestMatchers(HttpMethod.GET, "/tramos/**").hasAnyRole("OPERADOR", "TRANSPORTISTA")
                        .requestMatchers(HttpMethod.POST, "/tramos/**").hasRole("OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/tramos/*/iniciar", "/tramos/*/finalizar")
                        .hasAnyRole("OPERADOR", "TRANSPORTISTA")
                        .requestMatchers(HttpMethod.PUT, "/tramos/**").hasRole("OPERADOR")

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
