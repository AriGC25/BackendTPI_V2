package com.transportista.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaDTO {

    private Long id;
    private Long solicitudId;
    private Integer cantidadTramos;
    private Integer cantidadDepositos;
    private List<TramoDTO> tramos;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Campos para estimaciones
    private BigDecimal costoEstimado;
    private BigDecimal tiempoEstimadoHoras;
    private BigDecimal distanciaTotal;
    private String descripcion; // Descripción de la ruta (ej: "Ruta directa", "Ruta con 1 depósito")
}
