package com.transportista.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${services.solicitudes.url:http://solicitudes-service:8081}")
    private String urlSolicitudes;

    @Value("${services.logistica.url:http://logistica-service:8082}")
    private String urlLogistica;

    @Value("${services.tarifas.url:http://tarifas-service:8083}")
    private String urlTarifas;

    @Value("${services.tracking.url:http://tracking-service:8084}")
    private String urlTracking;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // ===== RUTAS DIRECTAS (sin /api/) =====

                // Solicitudes, Rutas y Tramos - Solicitudes Service
                .route("solicitudes-direct", r -> r
                        .path("/solicitudes/**", "/rutas/**", "/tramos/**")
                        .filters(f -> f.addRequestHeader("X-Gateway", "API-Gateway"))
                        .uri(urlSolicitudes)
                )

                // Depósitos y Camiones - Logística Service
                .route("logistica-direct", r -> r
                        .path("/depositos/**", "/camiones/**")
                        .filters(f -> f.addRequestHeader("X-Gateway", "API-Gateway"))
                        .uri(urlLogistica)
                )

                // Tarifas y Clientes - Tarifas Service
                .route("tarifas-direct", r -> r
                        .path("/tarifas/**", "/clientes/**")
                        .filters(f -> f.addRequestHeader("X-Gateway", "API-Gateway"))
                        .uri(urlTarifas)
                )

                // Tracking - Tracking Service
                .route("tracking-direct", r -> r
                        .path("/tracking/**")
                        .filters(f -> f.addRequestHeader("X-Gateway", "API-Gateway"))
                        .uri(urlTracking)
                )

                // ===== RUTAS CON PREFIJO /api/ (legacy) =====

                // Ruta para Solicitudes Service
                .route("solicitudes-service", r -> r
                        .path("/api/solicitudes/**")
                        .filters(f -> f
                                .stripPrefix(1) // Remueve solo /api
                                .addRequestHeader("X-Gateway", "API-Gateway")
                        )
                        .uri(urlSolicitudes)
                )

                // Ruta para Logística Service
                .route("logistica-service", r -> r
                        .path("/api/logistica/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway", "API-Gateway")
                        )
                        .uri(urlLogistica)
                )

                // Ruta para Tarifas Service
                .route("tarifas-service", r -> r
                        .path("/api/tarifas/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway", "API-Gateway")
                        )
                        .uri(urlTarifas)
                )

                // Ruta para Tracking Service
                .route("tracking-service", r -> r
                        .path("/api/tracking/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .addRequestHeader("X-Gateway", "API-Gateway")
                        )
                        .uri(urlTracking)
                )

                .build();
    }
}