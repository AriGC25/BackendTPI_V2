package com.transportista.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("solicitudes", r -> r
                .path("/solicitudes/**", "/rutas/**", "/tramos/**")
                .uri("http://solicitudes-service:8081"))
            .route("logistica", r -> r
                .path("/depositos/**", "/camiones/**")
                .uri("http://logistica-service:8082"))
            .route("tarifas", r -> r
                .path("/tarifas/**")
                .uri("http://tarifas-service:8083"))
            .route("tracking", r -> r
                .path("/tracking/**")
                .uri("http://tracking-service:8084"))
            .build();
    }
}
