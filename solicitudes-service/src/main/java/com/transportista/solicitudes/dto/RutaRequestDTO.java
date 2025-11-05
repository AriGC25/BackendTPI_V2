package com.transportista.solicitudes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaRequestDTO {

    @NotNull(message = "El ID de la solicitud es requerido")
    private Long solicitudId;

    private BigDecimal distanciaTotal;
    private BigDecimal costoEstimado;
    private Integer tiempoEstimado;

    private List<Long> depositosIds; // IDs de depósitos intermedios
}