package com.transportista.logistica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionDTO {
    
    private Long id;
    
    @NotBlank(message = "La patente es requerida")
    private String patente;
    
    @NotBlank(message = "El nombre del transportista es requerido")
    private String transportista;
    
    @NotNull(message = "La capacidad de peso es requerida")
    @Positive(message = "La capacidad de peso debe ser positiva")
    private BigDecimal capacidadPeso;
    
    @NotNull(message = "La capacidad de volumen es requerida")
    @Positive(message = "La capacidad de volumen debe ser positiva")
    private BigDecimal capacidadVolumen;
    
    @NotNull(message = "El costo por km es requerido")
    @Positive(message = "El costo por km debe ser positivo")
    private BigDecimal costoPorKm;
    
    private Boolean disponible;
    private Boolean activo;
}
