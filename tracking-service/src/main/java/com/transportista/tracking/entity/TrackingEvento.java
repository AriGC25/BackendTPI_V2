package com.transportista.tracking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contenedor_id", nullable = false)
    private Long contenedorId;

    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "ubicacion")
    private String ubicacion;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento;

    @PrePersist
    protected void onCreate() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}
