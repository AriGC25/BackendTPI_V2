package com.transportista.tarifas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifas")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de tramo es requerido")
    @Column(name = "tipo_tramo", nullable = false, length = 50)
    private String tipoTramo; // ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO

    @NotNull(message = "El costo por km es requerido")
    @Positive(message = "El costo por km debe ser positivo")
    @Column(name = "costo_por_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoPorKm;

    @NotNull(message = "El costo de gestión fija es requerido")
    @Positive(message = "El costo de gestión debe ser positivo")
    @Column(name = "gestion_fija", nullable = false, precision = 10, scale = 2)
    private BigDecimal gestionFija;

    @NotNull(message = "El consumo de combustible es requerido")
    @Positive(message = "El consumo debe ser positivo")
    @Column(name = "consumo_combustible_por_km", nullable = false, precision = 5, scale = 2)
    private BigDecimal consumoCombustiblePorKm; // Litros por km

    @NotNull(message = "El precio del combustible es requerido")
    @Positive(message = "El precio del combustible debe ser positivo")
    @Column(name = "precio_combustible_por_litro", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCombustiblePorLitro;

    @Column(name = "tarifa_estadia_deposito_por_dia", precision = 10, scale = 2)
    private BigDecimal tarifaEstadiaDepositoPorDia;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
