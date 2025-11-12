package com.transportista.solicitudes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContenedorDTO {

    private Long id;

    @NotNull(message = "El peso es requerido")
    @Positive(message = "El peso debe ser positivo")
    private BigDecimal peso;

    @NotNull(message = "El volumen es requerido")
    @Positive(message = "El volumen debe ser positivo")
    private BigDecimal volumen;

    private String estado;

    @NotNull(message = "El ID del cliente es requerido")
    private Long clienteId;

    private String descripcion;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}

