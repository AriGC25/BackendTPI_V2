package com.transportista.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorConUbicacionDTO {

    // Datos del contenedor
    private Long id;
    private BigDecimal peso;
    private BigDecimal volumen;
    private String estado;
    private Long clienteId;
    private String descripcion;
    private LocalDateTime fechaCreacion;

    // Datos de ubicación (desde Solicitud)
    private Long solicitudId;
    private String numeroSolicitud;
    private String estadoSolicitud;

    // Ubicación de origen
    private String direccionOrigen;
    private BigDecimal latitudOrigen;
    private BigDecimal longitudOrigen;

    // Ubicación de destino
    private String direccionDestino;
    private BigDecimal latitudDestino;
    private BigDecimal longitudDestino;

    // Ubicación actual (basada en el estado y tramos)
    private String ubicacionActual;
    private String descripcionUbicacion;

    // Información adicional
    private BigDecimal tiempoEstimadoHoras;
    private LocalDateTime fechaEstimadaEntrega;
}

