package com.transportista.logistica.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "camiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La patente es requerida")
    @Column(unique = true, nullable = false, length = 20)
    private String patente;

    @NotBlank(message = "El modelo es requerido")
    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;

    @NotNull(message = "La capacidad de peso es requerida")
    @Positive(message = "La capacidad de peso debe ser positiva")
    @Column(name = "capacidad_peso", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadPeso; // En kg

    @NotNull(message = "La capacidad de volumen es requerida")
    @Positive(message = "La capacidad de volumen debe ser positiva")
    @Column(name = "capacidad_volumen", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadVolumen; // En metros cúbicos

    @Column(name = "disponible")
    private Boolean disponible = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "activo")
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
        if (disponible == null) {
            disponible = true;
        }
    }
}
