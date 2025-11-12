package com.transportista.tracking.config;

import com.transportista.tracking.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrackingInitConfig {

    @Autowired
    private TrackingService trackingService;

    @Bean
    public CommandLineRunner initTrackingData() {
        return args -> {
            // Esperar 30 segundos para que los otros servicios estén listos
            try {
                Thread.sleep(30000);
                System.out.println("=================================================");
                System.out.println("Inicializando eventos de tracking...");
                int eventosCreados = trackingService.inicializarEventosExistentes();
                System.out.println("✓ Eventos de tracking inicializados: " + eventosCreados);
                System.out.println("=================================================");
            } catch (Exception e) {
                System.err.println("Error al inicializar eventos de tracking: " + e.getMessage());
            }
        };
    }
}

