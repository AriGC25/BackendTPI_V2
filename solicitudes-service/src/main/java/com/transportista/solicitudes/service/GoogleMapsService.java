package com.transportista.solicitudes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class GoogleMapsService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsService.class);

    @Value("${google.maps.api-key:AIzaSyDEMO_KEY}")
    private String apiKey;

    @Value("${google.maps.directions-url:https://maps.googleapis.com/maps/api/directions/json}")
    private String directionsUrl;

    private final WebClient webClient;

    public GoogleMapsService() {
        this.webClient = WebClient.builder().build();
    }

    /**
     * Calcula la distancia real entre dos puntos usando Google Maps Directions API
     */
    public BigDecimal calcularDistanciaReal(
            BigDecimal latOrigen, BigDecimal lonOrigen,
            BigDecimal latDestino, BigDecimal lonDestino) {

        try {
            String origin = latOrigen + "," + lonOrigen;
            String destination = latDestino + "," + lonDestino;

            String url = String.format("%s?origin=%s&destination=%s&key=%s",
                    directionsUrl, origin, destination, apiKey);

            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && "OK".equals(response.path("status").asText())) {
                int distanceInMeters = response.path("routes").get(0)
                        .path("legs").get(0)
                        .path("distance").path("value").asInt();

                BigDecimal distanceInKm = BigDecimal.valueOf(distanceInMeters)
                        .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);

                logger.info("Distancia calculada: {} km", distanceInKm);
                return distanceInKm;
            }

            // Fallback a fórmula de Haversine si falla la API
            logger.warn("Google Maps API no disponible, usando Haversine");
            return calcularDistanciaHaversine(latOrigen, lonOrigen, latDestino, lonDestino);

        } catch (Exception e) {
            logger.error("Error al consultar Google Maps API: {}", e.getMessage());
            return calcularDistanciaHaversine(latOrigen, lonOrigen, latDestino, lonDestino);
        }
    }

    /**
     * Fórmula de Haversine como fallback (distancia aproximada)
     */
    private BigDecimal calcularDistanciaHaversine(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2) {

        final int RADIO_TIERRA_KM = 6371;

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double deltaLon = Math.toRadians(lon2.subtract(lon1).doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distancia = RADIO_TIERRA_KM * c;

        return BigDecimal.valueOf(distancia).setScale(2, RoundingMode.HALF_UP);
    }
}