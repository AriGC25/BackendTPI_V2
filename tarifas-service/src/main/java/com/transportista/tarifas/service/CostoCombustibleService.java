package com.transportista.tarifas.service;

import com.transportista.tarifas.dto.CamionDTO;
import com.transportista.tarifas.dto.TarifaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class CostoCombustibleService {

    private final TarifaService tarifaService;

    /**
     * Calcula el costo de combustible para un tramo específico
     *
     * @param distanciaKm Distancia del tramo en kilómetros
     * @param consumoCombustiblePorKm Consumo del camión en litros por kilómetro
     * @param precioCombustiblePorLitro Precio del combustible por litro
     * @return El costo total de combustible
     */
    public BigDecimal calcularCostoCombustible(
            BigDecimal distanciaKm,
            BigDecimal consumoCombustiblePorKm,
            BigDecimal precioCombustiblePorLitro) {

        if (distanciaKm == null || consumoCombustiblePorKm == null || precioCombustiblePorLitro == null) {
            log.warn("Parámetros nulos en cálculo de combustible. Retornando costo cero.");
            return BigDecimal.ZERO;
        }

        // Fórmula: distancia * consumo_por_km * precio_por_litro
        BigDecimal litrosNecesarios = distanciaKm.multiply(consumoCombustiblePorKm);
        BigDecimal costoTotal = litrosNecesarios.multiply(precioCombustiblePorLitro);

        log.debug("Cálculo de combustible - Distancia: {} km, Consumo: {} L/km, Precio: ${}/L, Litros: {} L, Costo: ${}",
                distanciaKm, consumoCombustiblePorKm, precioCombustiblePorLitro, litrosNecesarios, costoTotal);

        return costoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el costo de combustible usando datos del camión y la tarifa del tipo de tramo
     *
     * @param distanciaKm Distancia del tramo en kilómetros
     * @param camionDTO Datos del camión con su consumo
     * @param tipoTramo Tipo de tramo para obtener la tarifa
     * @return El costo total de combustible
     */
    public BigDecimal calcularCostoCombustibleConCamion(
            BigDecimal distanciaKm,
            CamionDTO camionDTO,
            String tipoTramo) {

        if (camionDTO == null || camionDTO.getConsumoCombustiblePorKm() == null) {
            log.warn("Camión sin datos de consumo. Usando tarifa genérica.");
            return calcularCostoCombustibleConTarifa(distanciaKm, tipoTramo);
        }

        TarifaDTO tarifa = tarifaService.obtenerPorTipoTramo(tipoTramo);

        return calcularCostoCombustible(
                distanciaKm,
                camionDTO.getConsumoCombustiblePorKm(),
                tarifa.getPrecioCombustiblePorLitro()
        );
    }

    /**
     * Calcula el costo de combustible usando solo la tarifa genérica del tipo de tramo
     *
     * @param distanciaKm Distancia del tramo en kilómetros
     * @param tipoTramo Tipo de tramo para obtener la tarifa
     * @return El costo total de combustible
     */
    public BigDecimal calcularCostoCombustibleConTarifa(BigDecimal distanciaKm, String tipoTramo) {
        TarifaDTO tarifa = tarifaService.obtenerPorTipoTramo(tipoTramo);

        return calcularCostoCombustible(
                distanciaKm,
                tarifa.getConsumoCombustiblePorKm(),
                tarifa.getPrecioCombustiblePorLitro()
        );
    }

    /**
     * Calcula los litros de combustible necesarios para un trayecto
     *
     * @param distanciaKm Distancia en kilómetros
     * @param consumoCombustiblePorKm Consumo del camión en litros por kilómetro
     * @return Los litros necesarios
     */
    public BigDecimal calcularLitrosNecesarios(BigDecimal distanciaKm, BigDecimal consumoCombustiblePorKm) {
        if (distanciaKm == null || consumoCombustiblePorKm == null) {
            return BigDecimal.ZERO;
        }

        return distanciaKm.multiply(consumoCombustiblePorKm).setScale(2, RoundingMode.HALF_UP);
    }
}

