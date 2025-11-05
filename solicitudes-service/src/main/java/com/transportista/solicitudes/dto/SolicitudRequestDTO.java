package com.transportista.solicitudes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudRequestDTO {
    
    @NotNull(message = "El ID del cliente es requerido")
    private Long clienteId;
    
    @NotNull(message = "El peso del contenedor es requerido")
    @Positive(message = "El peso debe ser positivo")
    private BigDecimal pesoContenedor;
    
    @NotNull(message = "El volumen del contenedor es requerido")
    @Positive(message = "El volumen debe ser positivo")
    private BigDecimal volumenContenedor;
    
    private String descripcionContenedor;
    
    @NotNull(message = "La dirección de origen es requerida")
    private String direccionOrigen;
    
    @NotNull(message = "La latitud de origen es requerida")
    private BigDecimal latitudOrigen;
    
    @NotNull(message = "La longitud de origen es requerida")
    private BigDecimal longitudOrigen;
    
    @NotNull(message = "La dirección de destino es requerida")
    private String direccionDestino;
    
    @NotNull(message = "La latitud de destino es requerida")
    private BigDecimal latitudDestino;
    
    @NotNull(message = "La longitud de destino es requerida")
    private BigDecimal longitudDestino;
}
