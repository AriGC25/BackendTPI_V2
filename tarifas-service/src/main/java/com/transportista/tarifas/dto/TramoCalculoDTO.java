package com.transportista.tarifas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoCalculoDTO {
    private Long id;
    private Long camionId;
    private BigDecimal distanciaKm;
    private String tipoTramo;
}

