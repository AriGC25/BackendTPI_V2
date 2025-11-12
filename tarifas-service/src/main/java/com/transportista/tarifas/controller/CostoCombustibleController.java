package com.transportista.tarifas.controller;

import com.transportista.tarifas.dto.CamionDTO;
import com.transportista.tarifas.dto.TarifaDTO;
import com.transportista.tarifas.service.CostoCombustibleService;
import com.transportista.tarifas.service.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/combustible")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Combustible", description = "Cálculo de costos de combustible")
public class CostoCombustibleController {

    private final CostoCombustibleService costoCombustibleService;
    private final TarifaService tarifaService;
    private final WebClient.Builder webClientBuilder;

    @PostMapping("/calcular")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Calcular costo de combustible básico",
            description = "Calcula el costo de combustible dados los parámetros básicos")
    public ResponseEntity<Map<String, Object>> calcularCostoCombustible(
            @RequestParam BigDecimal distanciaKm,
            @RequestParam BigDecimal consumoPorKm,
            @RequestParam BigDecimal precioPorLitro) {

        log.info("Calculando costo de combustible - Distancia: {} km, Consumo: {} L/km, Precio: ${}/L",
                distanciaKm, consumoPorKm, precioPorLitro);

        BigDecimal costo = costoCombustibleService.calcularCostoCombustible(
                distanciaKm, consumoPorKm, precioPorLitro);

        BigDecimal litrosNecesarios = costoCombustibleService.calcularLitrosNecesarios(
                distanciaKm, consumoPorKm);

        Map<String, Object> response = new HashMap<>();
        response.put("distanciaKm", distanciaKm);
        response.put("consumoPorKm", consumoPorKm);
        response.put("precioPorLitro", precioPorLitro);
        response.put("litrosNecesarios", litrosNecesarios);
        response.put("costoTotal", costo);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/calcular/camion/{camionId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Calcular costo de combustible con datos del camión",
            description = "Calcula el costo de combustible usando el consumo del camión específico")
    public ResponseEntity<Map<String, Object>> calcularCostoCombustibleConCamion(
            @PathVariable Long camionId,
            @RequestParam BigDecimal distanciaKm,
            @RequestParam String tipoTramo) {

        log.info("Calculando costo de combustible - Camión ID: {}, Distancia: {} km, Tipo tramo: {}",
                camionId, distanciaKm, tipoTramo);

        // Obtener datos del camión
        CamionDTO camion = obtenerCamion(camionId);

        // Calcular costo
        BigDecimal costo = costoCombustibleService.calcularCostoCombustibleConCamion(
                distanciaKm, camion, tipoTramo);

        BigDecimal litrosNecesarios = costoCombustibleService.calcularLitrosNecesarios(
                distanciaKm, camion.getConsumoCombustiblePorKm());

        TarifaDTO tarifa = tarifaService.obtenerPorTipoTramo(tipoTramo);

        Map<String, Object> response = new HashMap<>();
        response.put("camionId", camionId);
        response.put("camionPatente", camion.getPatente());
        response.put("distanciaKm", distanciaKm);
        response.put("tipoTramo", tipoTramo);
        response.put("consumoPorKm", camion.getConsumoCombustiblePorKm());
        response.put("precioPorLitro", tarifa.getPrecioCombustiblePorLitro());
        response.put("litrosNecesarios", litrosNecesarios);
        response.put("costoTotal", costo);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/calcular/tarifa")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Calcular costo de combustible con tarifa genérica",
            description = "Calcula el costo de combustible usando valores genéricos de la tarifa del tipo de tramo")
    public ResponseEntity<Map<String, Object>> calcularCostoCombustibleConTarifa(
            @RequestParam BigDecimal distanciaKm,
            @RequestParam String tipoTramo) {

        log.info("Calculando costo de combustible con tarifa - Distancia: {} km, Tipo tramo: {}",
                distanciaKm, tipoTramo);

        BigDecimal costo = costoCombustibleService.calcularCostoCombustibleConTarifa(
                distanciaKm, tipoTramo);

        TarifaDTO tarifa = tarifaService.obtenerPorTipoTramo(tipoTramo);

        BigDecimal litrosNecesarios = costoCombustibleService.calcularLitrosNecesarios(
                distanciaKm, tarifa.getConsumoCombustiblePorKm());

        Map<String, Object> response = new HashMap<>();
        response.put("distanciaKm", distanciaKm);
        response.put("tipoTramo", tipoTramo);
        response.put("consumoPorKm", tarifa.getConsumoCombustiblePorKm());
        response.put("precioPorLitro", tarifa.getPrecioCombustiblePorLitro());
        response.put("litrosNecesarios", litrosNecesarios);
        response.put("costoTotal", costo);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/litros")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Calcular litros necesarios",
            description = "Calcula solo los litros de combustible necesarios para un trayecto")
    public ResponseEntity<Map<String, Object>> calcularLitrosNecesarios(
            @RequestParam BigDecimal distanciaKm,
            @RequestParam BigDecimal consumoPorKm) {

        log.info("Calculando litros necesarios - Distancia: {} km, Consumo: {} L/km",
                distanciaKm, consumoPorKm);

        BigDecimal litros = costoCombustibleService.calcularLitrosNecesarios(
                distanciaKm, consumoPorKm);

        Map<String, Object> response = new HashMap<>();
        response.put("distanciaKm", distanciaKm);
        response.put("consumoPorKm", consumoPorKm);
        response.put("litrosNecesarios", litros);

        return ResponseEntity.ok(response);
    }

    // ==================== MÉTODO AUXILIAR ====================

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
}

