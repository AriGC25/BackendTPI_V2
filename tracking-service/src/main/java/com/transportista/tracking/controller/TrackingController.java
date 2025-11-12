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
import java.util.Map;

@RestController
@RequestMapping("/tracking")
@Tag(name = "Tracking", description = "API para seguimiento de contenedores")
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    @PostMapping("/registrar")
    @Operation(summary = "Registrar evento de tracking", description = "Registra un nuevo evento de tracking para un contenedor")
    public ResponseEntity<TrackingEventoDTO> registrarEvento(@RequestBody Map<String, Object> eventoData) {
        Long contenedorId = ((Number) eventoData.get("contenedorId")).longValue();
        Long solicitudId = eventoData.get("solicitudId") != null ? ((Number) eventoData.get("solicitudId")).longValue() : null;
        String estado = (String) eventoData.get("estado");
        String ubicacion = (String) eventoData.get("ubicacion");
        String descripcion = (String) eventoData.get("descripcion");

        TrackingEventoDTO evento = trackingService.registrarEvento(contenedorId, solicitudId, estado, ubicacion, descripcion);
        return ResponseEntity.ok(evento);
    }

    @PostMapping("/inicializar")
    @Operation(summary = "Inicializar eventos existentes", description = "Genera eventos de tracking para solicitudes que no tienen eventos registrados")
    public ResponseEntity<Map<String, Object>> inicializarEventos() {
        int eventosCreados = trackingService.inicializarEventosExistentes();
        Map<String, Object> response = Map.of(
            "mensaje", "Eventos inicializados correctamente",
            "eventosCreados", eventosCreados
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contenedor/{contenedorId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Seguimiento por contenedor", description = "Obtiene el historial de eventos de un contenedor")
    public ResponseEntity<List<TrackingEventoDTO>> obtenerHistorialContenedor(@PathVariable("contenedorId") Long contenedorId) {
        List<TrackingEventoDTO> eventos = trackingService.obtenerHistorialContenedor(contenedorId);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Seguimiento por solicitud", description = "Obtiene el historial de eventos de una solicitud")
    public ResponseEntity<List<TrackingEventoDTO>> obtenerHistorialSolicitud(@PathVariable("solicitudId") Long solicitudId) {
        List<TrackingEventoDTO> eventos = trackingService.obtenerHistorialSolicitud(solicitudId);
        return ResponseEntity.ok(eventos);
    }
}
