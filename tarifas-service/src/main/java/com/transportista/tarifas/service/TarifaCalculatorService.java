package com.transportista.tarifas.service;

import com.transportista.tarifas.dto.*;
import com.transportista.tarifas.entity.Tarifa;
import com.transportista.tarifas.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifaCalculatorService {

    private static final Logger log = LoggerFactory.getLogger(TarifaCalculatorService.class);

    private final TarifaRepository tarifaRepository;
    private final WebClient.Builder webClientBuilder;
    private final CostoCombustibleService costoCombustibleService;

    /**
     * Calcula la tarifa COMPLETA de un envío según la regla de negocio:
     * - Cargo de gestión (base + por cantidad de tramos)
     * - Costo por kilómetro (diferenciado por capacidad)
     * - Costo de combustible
     * - Costo por estadía en depósito
     */
    public TarifaCalculadaResponse calcularTarifaCompleta(CalculoTarifaRequest request) {
        log.info("Iniciando cálculo de tarifa completa para solicitud ID: {}", request.getSolicitudId());

        // Obtener tarifa base activa del sistema
        Tarifa tarifa = tarifaRepository.findFirstByActivoTrueOrderByIdDesc()
            .orElseThrow(() -> new RuntimeException("No hay tarifas activas configuradas en el sistema"));

        BigDecimal costoTotal = BigDecimal.ZERO;
        List<DetalleTramoCosto> detalleTramos = new ArrayList<>();

        // 1. CARGO DE GESTIÓN (base + por cantidad de tramos)
        BigDecimal cargoGestion = calcularCargoGestion(tarifa, request.getTramos().size());
        costoTotal = costoTotal.add(cargoGestion);

        log.info("Cargo de gestión calculado: {} (Base: {} + {} tramos × {})",
            cargoGestion, tarifa.getGestionFija(),
            request.getTramos().size(),
            tarifa.getCargoGestionPorTramo() != null ? tarifa.getCargoGestionPorTramo() : BigDecimal.ZERO);

        // 2. COSTO POR KILÓMETRO + COMBUSTIBLE DE CADA TRAMO
        BigDecimal costoKilometrajeTotal = BigDecimal.ZERO;
        BigDecimal costoCombustibleTotal = BigDecimal.ZERO;

        for (TramoCalculoDTO tramo : request.getTramos()) {
            // Obtener datos del camión asignado
            CamionDTO camion = obtenerCamion(tramo.getCamionId());

            // Costo base por kilómetro (diferenciado por capacidad del contenedor)
            BigDecimal costoPorKm = obtenerCostoPorKmSegunCapacidad(
                tarifa, camion, request.getContenedor());

            BigDecimal costoKilometraje = costoPorKm.multiply(tramo.getDistanciaKm())
                .setScale(2, RoundingMode.HALF_UP);

            // Costo de combustible = (consumo del camión × distancia) × precio por litro
            BigDecimal costoCombustible = calcularCostoCombustible(
                camion, tramo.getDistanciaKm(), tarifa.getPrecioCombustiblePorLitro());

            costoKilometrajeTotal = costoKilometrajeTotal.add(costoKilometraje);
            costoCombustibleTotal = costoCombustibleTotal.add(costoCombustible);

            // Agregar detalle del tramo
            detalleTramos.add(DetalleTramoCosto.builder()
                .tramoId(tramo.getId())
                .tipoTramo(tramo.getTipoTramo())
                .distanciaKm(tramo.getDistanciaKm())
                .costoKilometraje(costoKilometraje)
                .costoCombustible(costoCombustible)
                .costoTotal(costoKilometraje.add(costoCombustible))
                .build());

            log.info("Tramo {}: Kilometraje {} + Combustible {} = {}",
                tramo.getId(), costoKilometraje, costoCombustible,
                costoKilometraje.add(costoCombustible));
        }

        costoTotal = costoTotal.add(costoKilometrajeTotal).add(costoCombustibleTotal);

        // 3. COSTO POR ESTADÍA EN DEPÓSITO
        BigDecimal costoEstadia = calcularCostoEstadia(
            tarifa, request.getDiasEstadia(), request.getTramos().size() - 1);
        costoTotal = costoTotal.add(costoEstadia);

        log.info("Costo por estadía ({} días, {} depósitos): {}",
            request.getDiasEstadia(), request.getTramos().size() - 1, costoEstadia);
        log.info("COSTO TOTAL CALCULADO: {}", costoTotal);

        return TarifaCalculadaResponse.builder()
            .tarifaTotal(costoTotal.setScale(2, RoundingMode.HALF_UP))
            .cargoGestion(cargoGestion)
            .costoKilometraje(costoKilometrajeTotal)
            .costoCombustible(costoCombustibleTotal)
            .costoEstadia(costoEstadia)
            .detalleTramos(detalleTramos)
            .mensaje("Tarifa calculada exitosamente")
            .build();
    }

    /**
     * Calcula tarifa APROXIMADA promediando entre camiones elegibles
     */
    public TarifaCalculadaResponse calcularTarifaAproximada(CalculoTarifaAproximadaRequest request) {
        log.info("Iniciando cálculo de tarifa aproximada para contenedor - Peso: {} kg, Volumen: {} m³",
            request.getContenedor().getPeso(), request.getContenedor().getVolumen());

        // Obtener camiones elegibles por características del contenedor
        List<CamionDTO> camionesElegibles = obtenerCamionesElegibles(
            request.getContenedor().getPeso(),
            request.getContenedor().getVolumen());

        if (camionesElegibles.isEmpty()) {
            throw new RuntimeException(
                "No hay camiones disponibles para las características del contenedor (Peso: " +
                request.getContenedor().getPeso() + " kg, Volumen: " +
                request.getContenedor().getVolumen() + " m³)");
        }

        Tarifa tarifa = tarifaRepository.findFirstByActivoTrueOrderByIdDesc()
            .orElseThrow(() -> new RuntimeException("No hay tarifas activas configuradas"));

        // Calcular costo con cada camión y promediar
        BigDecimal sumaTotal = BigDecimal.ZERO;
        List<BigDecimal> costosIndividuales = new ArrayList<>();

        for (CamionDTO camion : camionesElegibles) {
            BigDecimal costoConEsteCamion = calcularCostoConCamion(
                tarifa, camion, request.getContenedor(),
                request.getTramosEstimados(), request.getDiasEstadia());

            sumaTotal = sumaTotal.add(costoConEsteCamion);
            costosIndividuales.add(costoConEsteCamion);

            log.debug("Costo con camión {} ({}): {}",
                camion.getId(), camion.getPatente(), costoConEsteCamion);
        }

        BigDecimal tarifaPromedio = sumaTotal.divide(
            BigDecimal.valueOf(camionesElegibles.size()),
            2,
            RoundingMode.HALF_UP);

        log.info("Tarifa aproximada calculada: {} (promedio de {} camiones elegibles)",
            tarifaPromedio, camionesElegibles.size());

        return TarifaCalculadaResponse.builder()
            .tarifaTotal(tarifaPromedio)
            .mensaje(String.format("Tarifa aproximada basada en %d camiones elegibles",
                camionesElegibles.size()))
            .build();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private BigDecimal calcularCargoGestion(Tarifa tarifa, int cantidadTramos) {
        BigDecimal gestionBase = tarifa.getGestionFija();
        BigDecimal gestionPorTramo = tarifa.getCargoGestionPorTramo() != null
            ? tarifa.getCargoGestionPorTramo()
            : BigDecimal.ZERO;

        return gestionBase.add(
            gestionPorTramo.multiply(BigDecimal.valueOf(cantidadTramos)));
    }

    private BigDecimal obtenerCostoPorKmSegunCapacidad(
            Tarifa tarifa, CamionDTO camion, ContenedorDTO contenedor) {

        BigDecimal peso = contenedor.getPeso();
        BigDecimal volumen = contenedor.getVolumen();

        // Determinar tarifa base por PESO
        BigDecimal tarifaPorPeso = BigDecimal.ZERO;
        if (tarifa.getTarifaBasePesoLigero() != null && peso.compareTo(BigDecimal.valueOf(5000)) < 0) {
            tarifaPorPeso = tarifa.getTarifaBasePesoLigero();
        } else if (tarifa.getTarifaBasePesoMedio() != null && peso.compareTo(BigDecimal.valueOf(15000)) <= 0) {
            tarifaPorPeso = tarifa.getTarifaBasePesoMedio();
        } else if (tarifa.getTarifaBasePesoPesado() != null) {
            tarifaPorPeso = tarifa.getTarifaBasePesoPesado();
        }

        // Determinar tarifa base por VOLUMEN
        BigDecimal tarifaPorVolumen = BigDecimal.ZERO;
        if (tarifa.getTarifaBaseVolumenPequeno() != null && volumen.compareTo(BigDecimal.valueOf(20)) < 0) {
            tarifaPorVolumen = tarifa.getTarifaBaseVolumenPequeno();
        } else if (tarifa.getTarifaBaseVolumenMediano() != null && volumen.compareTo(BigDecimal.valueOf(50)) <= 0) {
            tarifaPorVolumen = tarifa.getTarifaBaseVolumenMediano();
        } else if (tarifa.getTarifaBaseVolumenGrande() != null) {
            tarifaPorVolumen = tarifa.getTarifaBaseVolumenGrande();
        }

        // Si no hay tarifas diferenciadas configuradas, usar la tarifa base
        if (tarifaPorPeso.compareTo(BigDecimal.ZERO) == 0 &&
            tarifaPorVolumen.compareTo(BigDecimal.ZERO) == 0) {
            return tarifa.getCostoPorKm();
        }

        // Usar la tarifa MÁS ALTA (la que más costo genera)
        return tarifaPorPeso.max(tarifaPorVolumen);
    }

    private BigDecimal calcularCostoCombustible(
            CamionDTO camion, BigDecimal distanciaKm, BigDecimal valorLitro) {

        if (camion.getConsumoCombustiblePorKm() == null) {
            log.warn("Camión {} no tiene consumo de combustible configurado, usando 0",
                camion.getId());
            return BigDecimal.ZERO;
        }

        // Usar el servicio dedicado para cálculo de combustible
        return costoCombustibleService.calcularCostoCombustible(
            distanciaKm,
            camion.getConsumoCombustiblePorKm(),
            valorLitro
        );
    }

    private BigDecimal calcularCostoEstadia(
            Tarifa tarifa, Integer diasEstadia, int cantidadDepositos) {

        if (tarifa.getTarifaEstadiaDepositoPorDia() == null ||
            diasEstadia == null ||
            cantidadDepositos <= 0) {
            return BigDecimal.ZERO;
        }

        // Costo por día × días × cantidad de depósitos intermedios
        return tarifa.getTarifaEstadiaDepositoPorDia()
            .multiply(BigDecimal.valueOf(diasEstadia))
            .multiply(BigDecimal.valueOf(cantidadDepositos))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularCostoConCamion(
            Tarifa tarifa,
            CamionDTO camion,
            ContenedorDTO contenedor,
            List<TramoCalculoDTO> tramos,
            Integer diasEstadia) {

        BigDecimal costo = BigDecimal.ZERO;

        // Cargo de gestión
        costo = costo.add(calcularCargoGestion(tarifa, tramos.size()));

        // Costo por kilómetro + combustible
        for (TramoCalculoDTO tramo : tramos) {
            BigDecimal costoPorKm = obtenerCostoPorKmSegunCapacidad(
                tarifa, camion, contenedor);

            BigDecimal costoKm = costoPorKm.multiply(tramo.getDistanciaKm());
            BigDecimal costoCombustible = calcularCostoCombustible(
                camion, tramo.getDistanciaKm(), tarifa.getPrecioCombustiblePorLitro());

            costo = costo.add(costoKm).add(costoCombustible);
        }

        // Costo de estadía
        costo = costo.add(calcularCostoEstadia(tarifa, diasEstadia, tramos.size() - 1));

        return costo.setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== INTEGRACIÓN CON LOGISTICA SERVICE ====================

    private CamionDTO obtenerCamion(Long camionId) {
        try {
            JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            String token = authentication.getToken().getTokenValue();
            String url = "http://logistica-service:8082/camiones/" + camionId;

            log.debug("Consultando camión ID {} en: {}", camionId, url);

            CamionDTO camion = webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(CamionDTO.class)
                .block();

            if (camion == null) {
                throw new RuntimeException("Camión no encontrado con ID: " + camionId);
            }

            return camion;
        } catch (Exception e) {
            log.error("Error al obtener camión ID {}: {}", camionId, e.getMessage());
            throw new RuntimeException("Error al consultar camión: " + e.getMessage(), e);
        }
    }

    private List<CamionDTO> obtenerCamionesElegibles(BigDecimal peso, BigDecimal volumen) {
        try {
            JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            String token = authentication.getToken().getTokenValue();
            String url = String.format(
                "http://logistica-service:8082/camiones/elegibles?peso=%s&volumen=%s",
                peso, volumen);

            log.debug("Consultando camiones elegibles en: {}", url);

            List<CamionDTO> camiones = webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToFlux(CamionDTO.class)
                .collectList()
                .block();

            return camiones != null ? camiones : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error al obtener camiones elegibles: {}", e.getMessage());
            // Si falla, retornar lista vacía en lugar de fallar completamente
            return new ArrayList<>();
        }
    }
}
