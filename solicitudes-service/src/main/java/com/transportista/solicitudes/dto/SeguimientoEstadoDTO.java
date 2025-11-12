package com.transportista.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoEstadoDTO {
    private Long id;
    private Long solicitudId;
    private String estado;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    private String usuarioRegistro;
}

