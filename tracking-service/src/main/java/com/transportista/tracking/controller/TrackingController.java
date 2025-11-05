package com.transportista.tracking.controller;

import com.transportista.tracking.dto.TrackingEventoDTO;
import com.transportista.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracking")
@Tag(name = "Tracking", description = "API para seguimiento de contenedores")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    @GetMapping("/contenedor/{contenedorId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Seguimiento por contenedor", description = "Obtiene el historial de eventos de un contenedor")
    public ResponseEntity<List<TrackingEventoDTO>> obtenerHistorialContenedor(@PathVariable Long contenedorId) {
        List<TrackingEventoDTO> eventos = trackingService.obtenerHistorialContenedor(contenedorId);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Seguimiento por solicitud", description = "Obtiene el historial de eventos de una solicitud")
    public ResponseEntity<List<TrackingEventoDTO>> obtenerHistorialSolicitud(@PathVariable Long solicitudId) {
        List<TrackingEventoDTO> eventos = trackingService.obtenerHistorialSolicitud(solicitudId);
        return ResponseEntity.ok(eventos);
    }
}
