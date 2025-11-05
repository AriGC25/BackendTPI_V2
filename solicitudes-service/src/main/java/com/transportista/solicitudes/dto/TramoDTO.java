package com.transportista.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoDTO {

    private Long id;
    private Long rutaId;
    private String tipoTramo;
    private Integer ordenTramo;
    private String estado;
    private String direccionOrigen;
    private BigDecimal latitudOrigen;
    private BigDecimal longitudOrigen;
    private String direccionDestino;
    private BigDecimal latitudDestino;
    private BigDecimal longitudDestino;
    private Long camionId;
    private String transportistaId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
