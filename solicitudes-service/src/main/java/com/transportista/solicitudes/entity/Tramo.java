package com.transportista.solicitudes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tramos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(name = "tipo_tramo", nullable = false, length = 50)
    private String tipoTramo; // ORIGEN_DEPOSITO, DEPOSITO_DEPOSITO, DEPOSITO_DESTINO, ORIGEN_DESTINO

    @Column(name = "numero_orden", nullable = false)
    private Integer ordenTramo; // Para mantener el orden de los tramos

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "estimado"; // estimado, asignado, iniciado, finalizado

    @NotNull(message = "La dirección de origen es requerida")
    @Column(name = "direccion_origen", nullable = false)
    private String direccionOrigen;

    @NotNull(message = "La latitud de origen es requerida")
    @Column(name = "latitud_origen", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitudOrigen;

    @NotNull(message = "La longitud de origen es requerida")
    @Column(name = "longitud_origen", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitudOrigen;

    @NotNull(message = "La dirección de destino es requerida")
    @Column(name = "direccion_destino", nullable = false)
    private String direccionDestino;

    @NotNull(message = "La latitud de destino es requerida")
    @Column(name = "latitud_destino", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitudDestino;

    @NotNull(message = "La longitud de destino es requerida")
    @Column(name = "longitud_destino", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitudDestino;

    @Column(name = "tiempo_estimado_minutos")
    private Integer tiempoEstimadoMinutos; // Tiempo estimado del tramo en minutos

    @Column(name = "distancia_km", precision = 10, scale = 2)
    private BigDecimal distanciaKm; // Distancia del tramo en kilómetros

    @Column(name = "costo_aproximado", precision = 10, scale = 2)
    private BigDecimal costoAproximado; // Costo aproximado del tramo

    @Column(name = "costo_real", precision = 10, scale = 2)
    private BigDecimal costoReal; // Costo real del tramo

    @Column(name = "dias_estadia_deposito")
    private Integer diasEstadiaDeposito; // Días de estadía en depósito (si aplica)

    @Column(name = "camion_id")
    private Long camionId;

    @Column(name = "transportista_id", length = 50)
    private String transportistaId;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) {
            estado = "estimado";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
