package com.transportista.tarifas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorDTO {
    private Long id;
    private BigDecimal peso;
    private BigDecimal volumen;
    private String descripcion;
}

