package com.transportista.tarifas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculoTarifaAproximadaRequest {
    private ContenedorDTO contenedor;
    private List<TramoCalculoDTO> tramosEstimados;
    private Integer diasEstadia;
}

