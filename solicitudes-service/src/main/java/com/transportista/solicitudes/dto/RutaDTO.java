package com.transportista.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
