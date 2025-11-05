package com.transportista.tarifas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TarifaDTO {

    private Long id;

    @NotNull(message = "El tipo de tramo es requerido")
    private String tipoTramo;

    @NotNull(message = "El costo por km es requerido")
    @Positive(message = "El costo por km debe ser positivo")
    private BigDecimal costoPorKm;

    @NotNull(message = "El costo de gestión fija es requerido")
    @Positive(message = "El costo de gestión debe ser positivo")
    private BigDecimal gestionFija;

    @NotNull(message = "El consumo de combustible es requerido")
    @Positive(message = "El consumo debe ser positivo")
    private BigDecimal consumoCombustiblePorKm;

    @NotNull(message = "El precio del combustible es requerido")
    @Positive(message = "El precio del combustible debe ser positivo")
    private BigDecimal precioCombustiblePorLitro;

    private BigDecimal tarifaEstadiaDepositoPorDia;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    private Boolean activo;
}
