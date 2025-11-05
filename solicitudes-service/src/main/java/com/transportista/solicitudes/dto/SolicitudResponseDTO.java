package com.transportista.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResponseDTO {
    
    private Long id;
    private String numeroSolicitud;
    private Long contenedorId;
    private Long clienteId;
    private String direccionOrigen;
    private BigDecimal latitudOrigen;
    private BigDecimal longitudOrigen;
    private String direccionDestino;
    private BigDecimal latitudDestino;
    private BigDecimal longitudDestino;
    private String estado;
    private BigDecimal costoTotal;
    private BigDecimal tiempoEstimadoHoras;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEstimadaEntrega;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
