package com.transportista.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//AGREGADAS
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

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
    private Integer tiempoEstimadoMinutos; // Tiempo estimado del tramo en minutos
    private BigDecimal distanciaKm; // Distancia del tramo en kilómetros
    private BigDecimal costoAproximado; // Costo aproximado del tramo
    private BigDecimal costoReal; // Costo real del tramo
    private Integer diasEstadiaDeposito; // Días de estadía en depósito (si aplica)
    private Long camionId;
    private String transportistaId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
