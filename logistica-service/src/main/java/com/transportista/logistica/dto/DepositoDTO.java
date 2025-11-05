package com.transportista.logistica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepositoDTO {
    
    private Long id;
    
    @NotBlank(message = "El nombre del depósito es requerido")
    private String nombre;
    
    @NotBlank(message = "La dirección es requerida")
    private String direccion;
    
    @NotNull(message = "La latitud es requerida")
    private BigDecimal latitud;
    
    @NotNull(message = "La longitud es requerida")
    private BigDecimal longitud;
    
    private Integer capacidadMaxima;
    private Integer capacidadActual;
    private Boolean activo;
}
