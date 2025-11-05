package com.transportista.solicitudes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TramoRequestDTO {

    @NotNull(message = "El ID de la ruta es requerido")
    private Long rutaId;

    @NotNull(message = "El número de orden es requerido")
    private Integer numeroOrden;

    private String direccionOrigen;
    private BigDecimal latitudOrigen;
    private BigDecimal longitudOrigen;

    private String direccionDestino;
    private BigDecimal latitudDestino;
    private BigDecimal longitudDestino;

    private String tipo; // ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO

    private Long depositoOrigenId;
    private Long depositoDestinoId;

    private BigDecimal distanciaKm;
    private BigDecimal costoEstimado;
}