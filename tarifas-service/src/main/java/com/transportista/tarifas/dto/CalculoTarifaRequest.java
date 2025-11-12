package com.transportista.tarifas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculoTarifaRequest {
    private Long solicitudId;
    private List<TramoCalculoDTO> tramos;
    private ContenedorDTO contenedor;
    private Integer diasEstadia;
}

