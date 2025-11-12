package com.transportista.tarifas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarifaCalculadaResponse {
    private BigDecimal tarifaTotal;
    private BigDecimal cargoGestion;
    private BigDecimal costoKilometraje;
    private BigDecimal costoCombustible;
    private BigDecimal costoEstadia;
    private List<DetalleTramoCosto> detalleTramos;
    private String mensaje;
}

