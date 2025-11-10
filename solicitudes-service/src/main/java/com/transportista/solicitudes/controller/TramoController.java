package com.transportista.solicitudes.controller;

import com.transportista.solicitudes.dto.TramoDTO;
import com.transportista.solicitudes.dto.TramoRequestDTO;
import com.transportista.solicitudes.service.TramoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/tramos")
@Tag(name = "Tramos", description = "Gestión de tramos de ruta")
public class TramoController {

    @Autowired
    private TramoService tramoService;

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar todos los tramos")
    public ResponseEntity<List<TramoDTO>> listarTodos() {
        return ResponseEntity.ok(tramoService.listarTodos());
    }

    @GetMapping("/transportista/{transportistaId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Ver tramos asignados a un transportista")
    public ResponseEntity<List<TramoDTO>> verTramosAsignados(@PathVariable("transportistaId") String transportistaId) {
        return ResponseEntity.ok(tramoService.listarTramosPorTransportista(transportistaId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener tramo por ID")
    public ResponseEntity<TramoDTO> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tramoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear nuevo tramo")
    public ResponseEntity<TramoDTO> crearTramo(@Valid @RequestBody TramoRequestDTO dto) {
        TramoDTO created = tramoService.crearTramo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/asignar-camion")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Asignar camión a tramo (valida capacidad)")
    public ResponseEntity<TramoDTO> asignarCamion(
            @PathVariable("id") Long id,
            @RequestParam("camionId") Long camionId,
            @RequestParam("transportistaId") String transportistaId) {
        TramoDTO updated = tramoService.asignarCamion(id, camionId, transportistaId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Iniciar tramo (registra fecha/hora de inicio)")
    public ResponseEntity<TramoDTO> iniciarTramo(@PathVariable("id") Long id) {
        TramoDTO updated = tramoService.iniciarTramo(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Finalizar tramo (registra fecha/hora de fin)")
    public ResponseEntity<TramoDTO> finalizarTramo(@PathVariable("id") Long id) {
        TramoDTO updated = tramoService.finalizarTramo(id);
        return ResponseEntity.ok(updated);
    }
}