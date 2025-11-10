package com.transportista.logistica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define la cadena de filtros de seguridad para la aplicación.
     * La configuración está diseñada para una API REST sin estado (STATELESS).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF, ya que no se usan sesiones basadas en cookies.
                .csrf(csrf -> csrf.disable())

                // 2. Configurar la autorización de peticiones HTTP.
                .authorizeHttpRequests(authorize -> authorize
                        // 2a. Permitir el acceso sin autenticación a las rutas de Swagger/OpenAPI.
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 2b. Para cualquier otra petición, se requiere autenticación.
                        .anyRequest().authenticated()
                )

                // 3. Configurar la gestión de sesiones para que sea sin estado.
                // Esto es fundamental para las APIs REST que usan tokens (como JWT).
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}