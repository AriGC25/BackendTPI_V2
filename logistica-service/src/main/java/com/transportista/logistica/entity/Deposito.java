package com.transportista.logistica.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "depositos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del depósito es requerido")
    @Column(nullable = false, unique = true)
    private String nombre;

    @NotBlank(message = "La dirección es requerida")
    @Column(nullable = false)
    private String direccion;

    @NotNull(message = "La latitud es requerida")
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitud;

    @NotNull(message = "La longitud es requerida")
    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitud;

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;

    @Column(name = "capacidad_actual")
    private Integer capacidadActual = 0;

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
        if (capacidadActual == null) {
            capacidadActual = 0;
        }
    }
}
