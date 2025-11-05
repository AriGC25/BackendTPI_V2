package com.transportista.solicitudes.service;

import com.transportista.solicitudes.entity.Solicitud;
import com.transportista.solicitudes.entity.Tramo;
import com.transportista.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculoCostoService {

    private final SolicitudRepository solicitudRepository;

    public BigDecimal calcularCostoTotal(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));

        BigDecimal costoTotal = BigDecimal.ZERO;

        // Obtener la ruta y sus tramos
        if (solicitud.getRuta() != null && solicitud.getRuta().getTramos() != null) {
            List<Tramo> tramos = solicitud.getRuta().getTramos();

            for (Tramo tramo : tramos) {
                BigDecimal costoTramo = calcularCostoTramo(tramo, solicitud);
                costoTotal = costoTotal.add(costoTramo);
            }
        }

        // Agregar costo base de gestión
        BigDecimal costoGestion = new BigDecimal("5000.00"); // Costo fijo de gestión
        costoTotal = costoTotal.add(costoGestion);

        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularCostoTramo(Tramo tramo, Solicitud solicitud) {
        BigDecimal costoTramo = BigDecimal.ZERO;

        // Calcular distancia estimada (simulado - en producción usaría Google Maps)
        BigDecimal distanciaKm = calcularDistanciaSimulada(
            tramo.getLatitudOrigen(), tramo.getLongitudOrigen(),
            tramo.getLatitudDestino(), tramo.getLongitudDestino()
        );

        // Costo por kilómetro según tipo de tramo
        BigDecimal costoPorKm = obtenerCostoPorKmSegunTipo(tramo.getTipoTramo());
        BigDecimal costoDistancia = distanciaKm.multiply(costoPorKm);
        costoTramo = costoTramo.add(costoDistancia);

        // Costo de combustible
        BigDecimal consumoPorKm = new BigDecimal("0.35"); // 0.35 litros por km
        BigDecimal precioCombustible = new BigDecimal("150.00"); // $150 por litro
        BigDecimal costoCombustible = distanciaKm.multiply(consumoPorKm).multiply(precioCombustible);
        costoTramo = costoTramo.add(costoCombustible);

        // Costo de estadía si hay fechas reales
        if (tramo.getFechaInicio() != null && tramo.getFechaFin() != null) {
            BigDecimal costoEstadia = calcularCostoEstadia(tramo);
            costoTramo = costoTramo.add(costoEstadia);
        }

        // Factor peso/volumen del contenedor
        BigDecimal factorPesoVolumen = calcularFactorPesoVolumen(solicitud);
        costoTramo = costoTramo.multiply(factorPesoVolumen);

        return costoTramo.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularDistanciaSimulada(BigDecimal latOrigen, BigDecimal lonOrigen,
                                               BigDecimal latDestino, BigDecimal lonDestino) {
        // Fórmula de Haversine simplificada para calcular distancia
        double lat1 = Math.toRadians(latOrigen.doubleValue());
        double lon1 = Math.toRadians(lonOrigen.doubleValue());
        double lat2 = Math.toRadians(latDestino.doubleValue());
        double lon2 = Math.toRadians(lonDestino.doubleValue());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = 6371 * c; // Radio de la Tierra en km

        return new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal obtenerCostoPorKmSegunTipo(String tipoTramo) {
        switch (tipoTramo) {
            case "ORIGEN_DESTINO":
                return new BigDecimal("120.00");
            case "ORIGEN_DEPOSITO":
                return new BigDecimal("100.00");
            case "DEPOSITO_DEPOSITO":
                return new BigDecimal("90.00");
            case "DEPOSITO_DESTINO":
                return new BigDecimal("110.00");
            default:
                return new BigDecimal("100.00");
        }
    }

    private BigDecimal calcularCostoEstadia(Tramo tramo) {
        if (tramo.getFechaInicio() == null || tramo.getFechaFin() == null) {
            return BigDecimal.ZERO;
        }

        Duration duracion = Duration.between(tramo.getFechaInicio(), tramo.getFechaFin());
        long dias = duracion.toDays();

        if (dias > 1) {
            // Cobrar estadía por días adicionales
            BigDecimal tarifaEstadia = new BigDecimal("500.00"); // $500 por día
            return tarifaEstadia.multiply(new BigDecimal(dias - 1));
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calcularFactorPesoVolumen(Solicitud solicitud) {
        if (solicitud.getContenedor() == null) {
            return BigDecimal.ONE;
        }

        BigDecimal peso = solicitud.getContenedor().getPeso();
        BigDecimal volumen = solicitud.getContenedor().getVolumen();

        BigDecimal factor = BigDecimal.ONE;

        // Factor por peso (> 20 toneladas)
        if (peso != null && peso.compareTo(new BigDecimal("20000")) > 0) {
            factor = factor.add(new BigDecimal("0.15")); // +15%
        }

        // Factor por volumen (> 50 m³)
        if (volumen != null && volumen.compareTo(new BigDecimal("50")) > 0) {
            factor = factor.add(new BigDecimal("0.10")); // +10%
        }

        return factor;
    }

    public BigDecimal calcularTiempoEstimado(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));

        BigDecimal tiempoTotalHoras = BigDecimal.ZERO;

        if (solicitud.getRuta() != null && solicitud.getRuta().getTramos() != null) {
            for (Tramo tramo : solicitud.getRuta().getTramos()) {
                BigDecimal distanciaKm = calcularDistanciaSimulada(
                    tramo.getLatitudOrigen(), tramo.getLongitudOrigen(),
                    tramo.getLatitudDestino(), tramo.getLongitudDestino()
                );

                // Velocidad promedio de 60 km/h
                BigDecimal velocidadPromedio = new BigDecimal("60");
                BigDecimal tiempoTramo = distanciaKm.divide(velocidadPromedio, 2, RoundingMode.HALF_UP);

                // Agregar tiempo de carga/descarga
                BigDecimal tiempoCargaDescarga = new BigDecimal("2.0"); // 2 horas
                tiempoTramo = tiempoTramo.add(tiempoCargaDescarga);

                tiempoTotalHoras = tiempoTotalHoras.add(tiempoTramo);
            }
        }

        return tiempoTotalHoras.setScale(1, RoundingMode.HALF_UP);
    }
}
