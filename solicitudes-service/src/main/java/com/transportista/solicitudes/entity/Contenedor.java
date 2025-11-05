package com.transportista.solicitudes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contenedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El peso es requerido")
    @Positive(message = "El peso debe ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal peso; // En toneladas

    @NotNull(message = "El volumen es requerido")
    @Positive(message = "El volumen debe ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volumen; // En metros cúbicos

    @Column(name = "estado", length = 50)
    private String estado; // PENDIENTE, EN_TRANSITO, EN_DEPOSITO, ENTREGADO

    @NotNull(message = "El ID del cliente es requerido")
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) {
            estado = "PENDIENTE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
