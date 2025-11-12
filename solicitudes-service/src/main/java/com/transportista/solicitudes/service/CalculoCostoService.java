package com.transportista.solicitudes.service;

import com.transportista.solicitudes.entity.*;
import com.transportista.solicitudes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
     * Incluye: costos por km, estadías en depósito y combustible de camiones específicos
     */
    public BigDecimal calcularCostoTotal(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        // Si no tiene ruta asignada, calcular estimación preliminar
        var rutaOpt = rutaRepository.findBySolicitudId(solicitudId);
        if (rutaOpt.isEmpty()) {
            return calcularCostoEstimadoPreliminar(solicitud);
        }

        Ruta ruta = rutaOpt.get();
        List<Tramo> tramos = tramoRepository.findByRutaIdOrderByOrdenTramo(ruta.getId());

        BigDecimal costoTotal = BigDecimal.ZERO;

        // 1. Sumar costos de todos los tramos (traslado por kilómetro + combustible por camión específico)
        for (Tramo tramo : tramos) {
            BigDecimal costoTramo = calcularCostoTramo(tramo);
            costoTotal = costoTotal.add(costoTramo);
        }

        // 2. Sumar costos de estadías en depósitos
        BigDecimal costoEstadias = calcularCostoEstadiasDetallado(tramos);
        costoTotal = costoTotal.add(costoEstadias);

        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el costo de un tramo individual incluyendo:
     * - Costo por kilómetro según tarifa del tipo de tramo
     * - Costo de combustible del camión específico asignado
     */
    private BigDecimal calcularCostoTramo(Tramo tramo) {
        BigDecimal costoTramo = BigDecimal.ZERO;

        // Obtener o calcular distancia del tramo
        BigDecimal distancia = tramo.getDistanciaKm();
        if (distancia == null || distancia.compareTo(BigDecimal.ZERO) <= 0) {
            distancia = googleMapsService.calcularDistanciaReal(
                    tramo.getLatitudOrigen(), tramo.getLongitudOrigen(),
                    tramo.getLatitudDestino(), tramo.getLongitudDestino()
            );
        }

        // 1. Costo por kilómetro según tarifa del tipo de tramo
        BigDecimal costoPorKm = obtenerCostoPorKm(tramo.getTipoTramo());
        BigDecimal costoDistancia = distancia.multiply(costoPorKm);
        costoTramo = costoTramo.add(costoDistancia);

        // 2. Costo de combustible del camión específico asignado
        if (tramo.getCamionId() != null) {
            BigDecimal costoCombustible = calcularCostoCombustibleCamionEspecifico(
                    tramo.getCamionId(), tramo.getTipoTramo(), distancia);
            costoTramo = costoTramo.add(costoCombustible);
        }

        return costoTramo;
    }

    /**
     * Calcula el costo de estadías en depósitos de forma detallada
     * Considera los días de estadía específicos de cada tramo
     */
    private BigDecimal calcularCostoEstadiasDetallado(List<Tramo> tramos) {
        BigDecimal costoTotalEstadias = BigDecimal.ZERO;

        for (Tramo tramo : tramos) {
            // Solo aplicar costo de estadía si el tramo termina en un depósito
            if (esTramoConEstadiaEnDeposito(tramo.getTipoTramo())) {
                // Obtener días de estadía (por defecto 1 día si no está especificado)
                Integer diasEstadia = tramo.getDiasEstadiaDeposito() != null
                        ? tramo.getDiasEstadiaDeposito()
                        : 1;

                // Obtener tarifa de estadía desde tarifas-service
                BigDecimal tarifaPorDia = obtenerTarifaEstadiaDeposito(tramo.getTipoTramo());

                BigDecimal costoEstadia = tarifaPorDia.multiply(BigDecimal.valueOf(diasEstadia));
                costoTotalEstadias = costoTotalEstadias.add(costoEstadia);
            }
        }

        return costoTotalEstadias;
    }

    /**
     * Determina si un tipo de tramo incluye estadía en depósito
     */
    private boolean esTramoConEstadiaEnDeposito(String tipoTramo) {
        return "ORIGEN_DEPOSITO".equals(tipoTramo) ||
                "DEPOSITO_DEPOSITO".equals(tipoTramo);
        // DEPOSITO_DESTINO no cuenta porque es el punto final de descarga
        // ORIGEN_DESTINO no tiene depósitos intermedios
    }

    /**
     * Calcula el costo de combustible usando el consumo específico del camión asignado
     */
    private BigDecimal calcularCostoCombustibleCamionEspecifico(Long camionId, String tipoTramo, BigDecimal distancia) {
        try {
            // Obtener datos del camión desde logistica-service
            String urlCamion = "http://logistica-service:8082/camiones/" + camionId;
            var camion = webClientBuilder.build()
                    .get()
                    .uri(urlCamion)
                    .retrieve()
                    .bodyToMono(CamionResponse.class)
                    .block();

            // Obtener precio de combustible desde tarifas-service
            String urlTarifa = "http://tarifas-service:8083/tarifas/tipo/" + tipoTramo;
            var tarifa = webClientBuilder.build()
                    .get()
                    .uri(urlTarifa)
                    .retrieve()
                    .bodyToMono(TarifaResponse.class)
                    .block();

            if (camion != null && camion.getConsumoCombustiblePorKm() != null &&
                    tarifa != null && tarifa.getPrecioCombustiblePorLitro() != null) {

                // Costo = distancia × consumo del camión × precio por litro
                BigDecimal litrosConsumidos = distancia.multiply(camion.getConsumoCombustiblePorKm());
                BigDecimal costoCombustible = litrosConsumidos.multiply(tarifa.getPrecioCombustiblePorLitro());

                return costoCombustible;
            }
        } catch (Exception e) {
            // Si falla la consulta, usar valores por defecto
        }

        // Valor por defecto si no se puede obtener datos específicos
        return calcularCostoCombustible(camionId, distancia);
    }

    /**
     * Obtiene la tarifa de estadía en depósito desde tarifas-service
     */
    private BigDecimal obtenerTarifaEstadiaDeposito(String tipoTramo) {
        try {
            String url = "http://tarifas-service:8083/tarifas/tipo/" + tipoTramo;
            var response = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(TarifaResponse.class)
                    .block();

            return response != null && response.getTarifaEstadiaDepositoPorDia() != null
                    ? response.getTarifaEstadiaDepositoPorDia()
                    : BigDecimal.valueOf(500); // Valor por defecto
        } catch (Exception e) {
            return BigDecimal.valueOf(500); // Valor por defecto si falla
        }
    }

    /**
     * Calcula el tiempo estimado de entrega en horas
     */
    public BigDecimal calcularTiempoEstimado(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        // Si no tiene ruta asignada, calcular estimación preliminar
        var rutaOpt = rutaRepository.findBySolicitudId(solicitudId);
        if (rutaOpt.isEmpty()) {
            return calcularTiempoEstimadoPreliminar(solicitud);
        }

        Ruta ruta = rutaOpt.get();
        List<Tramo> tramos = tramoRepository.findByRutaIdOrderByOrdenTramo(ruta.getId());

        BigDecimal tiempoTotal = BigDecimal.ZERO;
        final BigDecimal VELOCIDAD_PROMEDIO_KMH = BigDecimal.valueOf(60); // 60 km/h
        final BigDecimal TIEMPO_CARGA_DESCARGA_HORAS = BigDecimal.valueOf(2); // 2 horas por tramo
        final BigDecimal HORAS_POR_DIA_ESTADIA = BigDecimal.valueOf(24); // 24 horas por día

        for (Tramo tramo : tramos) {
            BigDecimal distancia = googleMapsService.calcularDistanciaReal(
                    tramo.getLatitudOrigen(), tramo.getLongitudOrigen(),
                    tramo.getLatitudDestino(), tramo.getLongitudDestino()
            );

            BigDecimal tiempoViaje = distancia.divide(VELOCIDAD_PROMEDIO_KMH, 2, RoundingMode.HALF_UP);
            tiempoTotal = tiempoTotal.add(tiempoViaje).add(TIEMPO_CARGA_DESCARGA_HORAS);

            // Agregar tiempo de estadía en depósito si aplica
            if (esTramoConEstadiaEnDeposito(tramo.getTipoTramo())) {
                Integer diasEstadia = tramo.getDiasEstadiaDeposito() != null
                        ? tramo.getDiasEstadiaDeposito()
                        : 1;
                BigDecimal tiempoEstadia = HORAS_POR_DIA_ESTADIA.multiply(BigDecimal.valueOf(diasEstadia));
                tiempoTotal = tiempoTotal.add(tiempoEstadia);
            }
        }

        return tiempoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula estimación preliminar de costo cuando no hay ruta asignada
     */
    private BigDecimal calcularCostoEstimadoPreliminar(Solicitud solicitud) {
        // Calcular distancia directa entre origen y destino
        BigDecimal distancia = googleMapsService.calcularDistanciaReal(
                solicitud.getLatitudOrigen(), solicitud.getLongitudOrigen(),
                solicitud.getLatitudDestino(), solicitud.getLongitudDestino()
        );

        // Costo base por km (aproximado)
        BigDecimal costoPorKm = BigDecimal.valueOf(120); // Tarifa base estimada
        BigDecimal costoDistancia = distancia.multiply(costoPorKm);

        // Costo de combustible estimado
        BigDecimal consumoPromedio = BigDecimal.valueOf(0.35); // Litros por km
        BigDecimal precioCombustible = BigDecimal.valueOf(150); // ARS por litro
        BigDecimal costoCombustible = distancia.multiply(consumoPromedio).multiply(precioCombustible);

        // Sumar costos base
        BigDecimal costoBase = costoDistancia.add(costoCombustible);

        // Aplicar factor por peso y volumen del contenedor
        BigDecimal factor = calcularFactorContenedor(solicitud.getContenedor());
        BigDecimal costoTotal = costoBase.multiply(factor);

        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula tiempo estimado preliminar cuando no hay ruta asignada
     */
    private BigDecimal calcularTiempoEstimadoPreliminar(Solicitud solicitud) {
        // Calcular distancia directa entre origen y destino
        BigDecimal distancia = googleMapsService.calcularDistanciaReal(
                solicitud.getLatitudOrigen(), solicitud.getLongitudOrigen(),
                solicitud.getLatitudDestino(), solicitud.getLongitudDestino()
        );

        // Velocidad promedio estimada
        final BigDecimal VELOCIDAD_PROMEDIO_KMH = BigDecimal.valueOf(60);
        final BigDecimal TIEMPO_CARGA_DESCARGA_HORAS = BigDecimal.valueOf(4); // 2h origen + 2h destino

        BigDecimal tiempoViaje = distancia.divide(VELOCIDAD_PROMEDIO_KMH, 2, RoundingMode.HALF_UP);
        BigDecimal tiempoTotal = tiempoViaje.add(TIEMPO_CARGA_DESCARGA_HORAS);

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
        private BigDecimal tarifaEstadiaDepositoPorDia;
        private BigDecimal precioCombustiblePorLitro;

        public BigDecimal getCostoPorKm() {
            return costoPorKm;
        }

        public void setCostoPorKm(BigDecimal costoPorKm) {
            this.costoPorKm = costoPorKm;
        }

        public BigDecimal getTarifaEstadiaDepositoPorDia() {
            return tarifaEstadiaDepositoPorDia;
        }

        public void setTarifaEstadiaDepositoPorDia(BigDecimal tarifaEstadiaDepositoPorDia) {
            this.tarifaEstadiaDepositoPorDia = tarifaEstadiaDepositoPorDia;
        }

        public BigDecimal getPrecioCombustiblePorLitro() {
            return precioCombustiblePorLitro;
        }

        public void setPrecioCombustiblePorLitro(BigDecimal precioCombustiblePorLitro) {
            this.precioCombustiblePorLitro = precioCombustiblePorLitro;
        }
    }

    static class CamionResponse {
        private BigDecimal consumoCombustiblePorKm;

        public BigDecimal getConsumoCombustiblePorKm() {
            return consumoCombustiblePorKm;
        }

        public void setConsumoCombustiblePorKm(BigDecimal consumoCombustiblePorKm) {
            this.consumoCombustiblePorKm = consumoCombustiblePorKm;
        }
    }
}
