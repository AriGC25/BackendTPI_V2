package com.transportista.tarifas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleTramoCosto {
    private Long tramoId;
    private String tipoTramo;
    private BigDecimal distanciaKm;
    private BigDecimal costoKilometraje;
    private BigDecimal costoCombustible;
    private BigDecimal costoTotal;
}

