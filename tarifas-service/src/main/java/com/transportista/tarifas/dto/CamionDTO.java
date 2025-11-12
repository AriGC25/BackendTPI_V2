package com.transportista.tarifas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionDTO {
    private Long id;
    private String patente;
    private String modelo;
    private BigDecimal capacidadPeso;
    private BigDecimal capacidadVolumen;
    private BigDecimal consumoCombustiblePorKm;
    private Boolean disponible;
}

