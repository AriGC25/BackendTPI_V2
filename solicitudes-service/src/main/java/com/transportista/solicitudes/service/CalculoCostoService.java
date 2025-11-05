package com.transportista.solicitudes.service;

import com.transportista.solicitudes.entity.*;
import com.transportista.solicitudes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CalculoCostoService {

    @Autowired
    private GoogleMapsService googleMapsService;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private TramoRepository tramoRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Calcula el costo total de una solicitud con todas las tarifas aplicadas
     */
    public BigDecimal calcularCostoTotal(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        Ruta ruta = rutaRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No hay ruta asignada"));

        List<Tramo> tramos = tramoRepository.findByRutaIdOrderByOrdenTramo(ruta.getId());

        BigDecimal costoTotal = BigDecimal.ZERO;

        // 1. Costo por kilómetro de cada tramo
        for (Tramo tramo : tramos) {
            BigDecimal distancia = googleMapsService.calcularDistanciaReal(
                    tramo.getLatitudOrigen(), tramo.getLongitudOrigen(),
                    tramo.getLatitudDestino(), tramo.getLongitudDestino()
            );

            // Obtener tarifa del tipo de tramo desde tarifas-service
            BigDecimal costoPorKm = obtenerCostoPorKm(tramo.getTipoTramo());
            BigDecimal costoTramo = distancia.multiply(costoPorKm);

            // Si tiene camión asignado, agregar costo de combustible
            if (tramo.getCamionId() != null) {
                BigDecimal costoCombustible = calcularCostoCombustible(
                        tramo.getCamionId(), distancia);
                costoTramo = costoTramo.add(costoCombustible);
            }

            costoTotal = costoTotal.add(costoTramo);
        }

        // 2. Costo de estadías en depósitos
        BigDecimal costoEstadias = calcularCostoEstadias(tramos);
        costoTotal = costoTotal.add(costoEstadias);

        // 3. Factor por peso y volumen del contenedor
        BigDecimal factor = calcularFactorContenedor(solicitud.getContenedor());
        costoTotal = costoTotal.multiply(factor);

        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el tiempo estimado de entrega en horas
     */
    public BigDecimal calcularTiempoEstimado(Long solicitudId) {
        Ruta ruta = rutaRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("No hay ruta asignada"));

        List<Tramo> tramos = tramoRepository.findByRutaIdOrderByOrdenTramo(ruta.getId());

        BigDecimal tiempoTotal = BigDecimal.ZERO;
        final BigDecimal VELOCIDAD_PROMEDIO_KMH = BigDecimal.valueOf(60); // 60 km/h
        final BigDecimal TIEMPO_CARGA_DESCARGA_HORAS = BigDecimal.valueOf(2); // 2 horas

        for (Tramo tramo : tramos) {
            BigDecimal distancia = googleMapsService.calcularDistanciaReal(
                    tramo.getLatitudOrigen(), tramo.getLongitudOrigen(),
                    tramo.getLatitudDestino(), tramo.getLongitudDestino()
            );

            BigDecimal tiempoViaje = distancia.divide(VELOCIDAD_PROMEDIO_KMH, 2, RoundingMode.HALF_UP);
            tiempoTotal = tiempoTotal.add(tiempoViaje).add(TIEMPO_CARGA_DESCARGA_HORAS);
        }

        return tiempoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal obtenerCostoPorKm(String tipoTramo) {
        try {
            // Llamar a tarifas-service para obtener la tarifa activa
            String url = "http://tarifas-service:8083/tarifas/tipo/" + tipoTramo;

            var response = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(TarifaResponse.class)
                    .block();

            return response != null ? response.getCostoPorKm() : BigDecimal.valueOf(100);
        } catch (Exception e) {
            // Valor por defecto si falla
            return BigDecimal.valueOf(100);
        }
    }

    private BigDecimal calcularCostoCombustible(Long camionId, BigDecimal distancia) {
        try {
            String url = "http://logistica-service:8082/camiones/" + camionId;

            var camion = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(CamionResponse.class)
                    .block();

            if (camion != null && camion.getConsumoCombustiblePorKm() != null) {
                BigDecimal litrosConsumidos = distancia.multiply(camion.getConsumoCombustiblePorKm());
                BigDecimal precioPorLitro = BigDecimal.valueOf(150); // Precio fijo o de tarifa
                return litrosConsumidos.multiply(precioPorLitro);
            }
        } catch (Exception e) {
            // Ignorar si no se puede obtener
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calcularCostoEstadias(List<Tramo> tramos) {
        final BigDecimal COSTO_DIA_DEPOSITO = BigDecimal.valueOf(500);
        int cantidadDepositosIntermedios = (int) tramos.stream()
                .filter(t -> "DEPOSITO_DEPOSITO".equals(t.getTipoTramo()) ||
                        "ORIGEN_DEPOSITO".equals(t.getTipoTramo()))
                .count();

        return COSTO_DIA_DEPOSITO.multiply(BigDecimal.valueOf(cantidadDepositosIntermedios));
    }

    private BigDecimal calcularFactorContenedor(Contenedor contenedor) {
        // Factor basado en peso y volumen
        BigDecimal pesoFactor = contenedor.getPeso().divide(BigDecimal.valueOf(20000), 4, RoundingMode.HALF_UP);
        BigDecimal volumenFactor = contenedor.getVolumen().divide(BigDecimal.valueOf(50), 4, RoundingMode.HALF_UP);

        BigDecimal factor = BigDecimal.ONE.add(pesoFactor).add(volumenFactor);
        return factor.min(BigDecimal.valueOf(2.0)); // Máximo 2x el costo base
    }

    // DTOs internos
    static class TarifaResponse {
        private BigDecimal costoPorKm;
        public BigDecimal getCostoPorKm() { return costoPorKm; }
        public void setCostoPorKm(BigDecimal costoPorKm) { this.costoPorKm = costoPorKm; }
    }

    static class CamionResponse {
        private BigDecimal consumoCombustiblePorKm;
        public BigDecimal getConsumoCombustiblePorKm() { return consumoCombustiblePorKm; }
        public void setConsumoCombustiblePorKm(BigDecimal consumoCombustiblePorKm) {
            this.consumoCombustiblePorKm = consumoCombustiblePorKm;
        }
    }
}