package com.transportista.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventoDTO {
    
    private Long id;
    private Long contenedorId;
    private Long solicitudId;
    private String estado;
    private String ubicacion;
    private String descripcion;
    private LocalDateTime fechaEvento;
}
